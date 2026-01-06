package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.data.*
import com.rocasspb.avaawaand.utils.AvalancheConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class AvalancheLogicTest {

    @Test
    fun `processRegionElevations with problems uses danger rating elevation`() {
        val region = Region("testRegion", "Test Region")
        val dangerRating = DangerRating("3", null, Elevation("2000", "4000"))
        
        // We need a problem that overlaps with the danger rating for it to be picked up
        // The problem aspect/elevation will further refine it, but let's make it cover the whole range for this test
        val problem = AvalancheProblem(
            problemType = "any",
            elevation = Elevation("1000", "5000"),
            validTimePeriod = null,
            snowpackStability = null,
            frequency = null,
            avalancheSize = null,
            aspects = listOf("N")
        )
        
        val bulletin = AvalancheData(
            bulletinID = "b1",
            publicationTime = "2023-01-01",
            validTime = ValidTime("2023-01-01", "2023-01-02"),
            avalancheActivity = null,
            snowpackStructure = null,
            dangerRatings = listOf(dangerRating),
            avalancheProblems = listOf(problem),
            tendency = null,
            weatherForecast = null,
            weatherReview = null,
            regions = listOf(region)
        )

        val bands = AvalancheLogic.processRegionElevations(listOf(bulletin))
        
        // logic: 
        // rMin=2000, rMax=4000.
        // Problem pMin=0, pMax=5000. Overlaps.
        // Union logic:
        // unionMin starts at rMax(4000). min(4000, 0) -> 0.
        // unionMax starts at rMin(2000). max(2000, 5000) -> 5000.
        // Final band: 0 - 5000.
        
        // Wait, the union logic expands the band to the problem size?
        // My implementation:
        // var unionMin = rMax
        // var unionMax = rMin
        // matchingProblems.forEach { ... unionMin = min(unionMin, pMin) ... }
        // rMin = unionMin
        
        // If the intention is to intersect, the logic is weird.
        // In TS:
        // var minElev = rMax; var maxElev = rMin;
        // matchingProblems.forEach ...
        // minElev = Math.min(minElev, pMin);
        // maxElev = Math.max(maxElev, pMax);
        // rMin = minElev; rMax = maxElev;
        
        // This calculates the Union of all matching problems' elevations.
        // AND then assigns it to rMin/rMax.
        // So effectively, the band becomes the Union of problems, IGNORING the original DangerRating elevation (except that we filtered problems based on overlap with it).
        
        // So in this test case:
        // DangerRating 2000-4000.
        // Problem 0-5000.
        // They overlap.
        // Result band becomes 0-5000.
        
        assertEquals(1, bands.size)
        assertEquals("testRegion", bands[0].regionID)
        assertEquals(1000.0, bands[0].minElev, 0.01)
        assertEquals(5000.0, bands[0].maxElev, 0.01)
    }

    @Test
    fun `processRegionElevations without problems uses fallback`() {
        val region = Region("testRegion", "Test Region")
        val dangerRating = DangerRating("3", null, Elevation("2000", "4000"))
        
        val bulletin = AvalancheData(
            bulletinID = "b1",
            publicationTime = "",
            validTime = ValidTime("", ""),
            avalancheActivity = null,
            snowpackStructure = null,
            dangerRatings = listOf(dangerRating),
            avalancheProblems = emptyList(),
            tendency = null,
            weatherForecast = null,
            weatherReview = null,
            regions = listOf(region)
        )

        val bands = AvalancheLogic.processRegionElevations(listOf(bulletin))
        
        // Fallback uses 0 to DEFAULT_MAX
        assertEquals(1, bands.size)
        assertEquals("testRegion", bands[0].regionID)
        assertEquals(0.0, bands[0].minElev, 0.01)
        assertEquals(AvalancheConfig.DEFAULT_MAX_ELEVATION, bands[0].maxElev, 0.01)
    }

    @Test
    fun `processRegionElevations handles problems intersection`() {
        val region = Region("testRegion", "Test Region")
        val dangerRating = DangerRating("3", null, Elevation("0", "4000"))
        
        // Problem exists between 2000 and 3000
        val problem = AvalancheProblem(
            problemType = "wind-slab",
            elevation = Elevation("2000", "3000"),
            validTimePeriod = null,
            snowpackStability = null,
            frequency = null,
            avalancheSize = null,
            aspects = listOf("N", "NE")
        )
        
        val bulletin = AvalancheData(
            bulletinID = "b1",
            publicationTime = "",
            validTime = ValidTime("", ""),
            avalancheActivity = null,
            snowpackStructure = null,
            dangerRatings = listOf(dangerRating),
            avalancheProblems = listOf(problem),
            tendency = null,
            weatherForecast = null,
            weatherReview = null,
            regions = listOf(region)
        )

        val bands = AvalancheLogic.processRegionElevations(listOf(bulletin))
        
        // DangerRating 0-4000.
        // Problem 2000-3000.
        // Overlap.
        // Union of problems: 2000-3000.
        // Result: 2000-3000.
        
        assertEquals(1, bands.size)
        assertEquals("testRegion", bands[0].regionID)
        assertEquals(2000.0, bands[0].minElev, 0.01)
        assertEquals(3000.0, bands[0].maxElev, 0.01)
        assertEquals(listOf("N", "NE"), bands[0].validAspects)
    }
    
    @Test
    fun `adjustElevationForTreeline handles treeline keyword`() {
        val problem = AvalancheProblem(
            problemType = "wind-slab",
            elevation = Elevation("treeline", "4000"), // Treeline is 2000
            validTimePeriod = null,
            snowpackStability = null,
            frequency = null,
            avalancheSize = null,
            aspects = null
        )
        
        val (min, max) = AvalancheLogic.adjustElevationForTreeline(1000.0, 3000.0, listOf(problem))
        // lb="treeline" -> min = max(1000, 2000) = 2000
        // ub="4000" -> max = 3000 (unchanged by loop logic unless ub was treeline)
        
        assertEquals(2000.0, min, 0.01)
        assertEquals(3000.0, max, 0.01)
    }
}
