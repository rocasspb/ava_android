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
        // Problem pMin=1000, pMax=5000. Overlaps.
        // Union logic:
        // unionMin starts at rMax(4000). min(4000, 1000) -> 1000.
        // unionMax starts at rMin(2000). max(2000, 5000) -> 5000.
        // Final band: 1000 - 5000.
        
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

    @Test
    fun `processRegionElevations handles mixed treeline and numerical elevation bands`() {
        val region = Region("region1", "Region 1")
        
        // Danger rating from treeline up (2000 to 4000)
        val dangerRating = DangerRating("3", null, Elevation("treeline", null))
        
        // Problem 1: from treeline to 3000
        val problem1 = AvalancheProblem(
            problemType = "wind-slab",
            elevation = Elevation("treeline", "3000"),
            validTimePeriod = null,
            snowpackStability = null,
            frequency = null,
            avalancheSize = null,
            aspects = listOf("N", "NE")
        )
        
        // Problem 2: numerical band 2500 to 3500
        val problem2 = AvalancheProblem(
            problemType = "persistent-weak-layer",
            elevation = Elevation("2500", "3500"),
            validTimePeriod = null,
            snowpackStability = null,
            frequency = null,
            avalancheSize = null,
            aspects = listOf("W", "NW")
        )
        
        val bulletin = AvalancheData(
            bulletinID = "b1",
            publicationTime = "",
            validTime = ValidTime("", ""),
            avalancheActivity = null,
            snowpackStructure = null,
            dangerRatings = listOf(dangerRating),
            avalancheProblems = listOf(problem1, problem2),
            tendency = null,
            weatherForecast = null,
            weatherReview = null,
            regions = listOf(region)
        )

        val bands = AvalancheLogic.processRegionElevations(listOf(bulletin))
        
        // Danger: 2000 - 4000 (treeline is 2000, null max is 4000)
        // Problem 1: 2000 - 3000 (overlaps)
        // Problem 2: 2500 - 3500 (overlaps)
        // Union min: min(2000, 2500) = 2000
        // Union max: max(3000, 3500) = 3500
        
        assertEquals(1, bands.size)
        assertEquals(2000.0, bands[0].minElev, 0.01)
        assertEquals(3500.0, bands[0].maxElev, 0.01)
        val expectedAspects = listOf("N", "NE", "W", "NW")
        assertEquals(expectedAspects.sorted(), bands[0].validAspects?.sorted())
    }
}
