package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.test.*

class RasterGeneratorTest {

    private class ConstantElevationProvider(val elevation: Int) : ElevationProvider {
        override fun getElevation(point: GeometryUtils.Point): Int = elevation
    }

    @Test
    fun testGenerateRasterReturnsNullForInvalidBounds() {
        val bounds = GeometryUtils.Bounds(10.0, 10.0, 45.0, 45.0)
        val raster = RasterGenerator.generateRaster(emptyList(), bounds, ConstantElevationProvider(1000))
        assertNull(raster)
    }

    @Test
    fun testGenerateRasterGeneratesWithCorrectColor() {
        val bounds = GeometryUtils.Bounds(10.0, 11.0, 45.0, 46.0)
        val ruleBounds = GeometryUtils.Bounds(10.0, 11.0, 45.0, 46.0)
        
        val rule = GenerationRule(
            bounds = ruleBounds,
            geometry = null,
            minElev = 500,
            maxElev = 2000,
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "3")
        )
        
        val raster = RasterGenerator.generateRaster(
            listOf(rule),
            bounds,
            ConstantElevationProvider(1000)
        )
        
        assertNotNull(raster)
        val pixel = raster.pixels[raster.pixels.size / 2]
        assertEquals(0xFFFF0000.toInt(), pixel)
    }

    @Test
    fun testGenerateRasterFiltersByElevation() {
        val bounds = GeometryUtils.Bounds(10.0, 11.0, 45.0, 46.0)
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 2000,
            maxElev = 3000,
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "3")
        )
        
        val raster = RasterGenerator.generateRaster(
            listOf(rule),
            bounds,
            ConstantElevationProvider(1000)
        )
        
        assertNotNull(raster)
        val pixel = raster.pixels[raster.pixels.size / 2]
        assertEquals(0x00000000, pixel)
    }

    @Test
    fun testGenerateRasterAppliesAspectFiltering() {
        val bounds = GeometryUtils.Bounds(11.7, 11.8, 47.2, 47.3)
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 0,
            maxElev = 4000,
            validAspects = listOf("N"),
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "low")
        )
        
        val northFacingProvider = object : ElevationProvider {
            override fun getElevation(point: GeometryUtils.Point): Int {
                return (1000 - (point.y - 47.25) * 100000).toInt()
            }
        }
        
        val raster = RasterGenerator.generateRaster(listOf(rule), bounds, northFacingProvider)
        assertNotNull(raster)
        val pixel = raster.pixels[raster.pixels.size / 2]
        assertEquals(0xFFFF0000.toInt(), pixel, "Should be Red on North aspect")
        
        val southFacingRule = rule.copy(validAspects = listOf("S"))
        val raster2 = RasterGenerator.generateRaster(listOf(southFacingRule), bounds, northFacingProvider)
        assertNotNull(raster2)
        assertEquals(0x00000000, raster2.pixels[raster2.pixels.size / 2], "Should be Transparent on wrong aspect")
    }

    @Test
    fun testGenerateRasterAppliesSteepnessLogic() {
        val bounds = GeometryUtils.Bounds(11.7, 11.8, 47.2, 47.3)
        
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 0,
            maxElev = 4000,
            applySteepnessLogic = true,
            color = "#FF9900",
            properties = RuleProperties(dangerLevel = "considerable")
        )

        val steepProvider = object : ElevationProvider {
            override fun getElevation(point: GeometryUtils.Point): Int {
                return (1000.0 + (point.y - 47.25) * 80000.0).toInt()
            }
        }

        val raster = RasterGenerator.generateRaster(listOf(rule), bounds, steepProvider)
        assertNotNull(raster)
        
        val pixel = raster.pixels[raster.pixels.size / 2]
        val highColor = 0xFFFF0000.toInt()
        assertEquals(highColor, pixel, "Should be red for slope >= 35 with danger 3")
    }

    @Test
    fun testGenerateRasterWithOverlappingGeometries() {
        val bounds = GeometryUtils.Bounds(0.0, 10.0, 0.0, 10.0)
        
        // Rule 1: Square (2,2) to (6,6), Color Red (#FF0000)
        val rule1 = GenerationRule(
            bounds = GeometryUtils.Bounds(2.0, 6.0, 2.0, 6.0),
            geometry = com.rocasspb.avaawaand.data.Geometry(
                type = "Polygon",
                coordinates = listOf(listOf(listOf(
                    listOf(2.0, 2.0),
                    listOf(6.0, 2.0),
                    listOf(6.0, 6.0),
                    listOf(2.0, 6.0),
                    listOf(2.0, 2.0)
                )))
            ),
            minElev = 0,
            maxElev = 5000,
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "4") // High
        )
        
        // Rule 2: Square (4,4) to (8,8), Color Orange (#FF9900)
        val rule2 = GenerationRule(
            bounds = GeometryUtils.Bounds(4.0, 8.0, 4.0, 8.0),
            geometry = com.rocasspb.avaawaand.data.Geometry(
                type = "Polygon",
                coordinates = listOf(listOf(listOf(
                    listOf(4.0, 4.0),
                    listOf(8.0, 4.0),
                    listOf(8.0, 8.0),
                    listOf(4.0, 8.0),
                    listOf(4.0, 4.0)
                )))
            ),
            minElev = 0,
            maxElev = 5000,
            color = "#FF9900",
            properties = RuleProperties(dangerLevel = "2") // Moderate
        )
        
        val raster = RasterGenerator.generateRaster(
            listOf(rule1, rule2),
            bounds,
            ConstantElevationProvider(1000)
        )
        
        assertNotNull(raster)
        
        // Point (3,3) -> Only Rule 1 -> Red
        val idx33 = getPixelIndex(3.0, 3.0, bounds, raster.width, raster.height)
        assertEquals(0xFFFF0000.toInt(), raster.pixels[idx33], "Point (3.0, 3.0) should be Red")
        
        // Point (7,7) -> Only Rule 2 -> Orange
        val idx77 = getPixelIndex(7.0, 7.0, bounds, raster.width, raster.height)
        assertEquals(0xFFFF9900.toInt(), raster.pixels[idx77], "Point (7.0, 7.0) should be Orange")
        
        // Point (5,5) -> Both rules -> Rule 1 wins (higher DL)
        val idx55 = getPixelIndex(5.0, 5.0, bounds, raster.width, raster.height)
        assertEquals(0xFFFF0000.toInt(), raster.pixels[idx55], "Point (5.0, 5.0) should be Red (higher danger level wins)")
    }
    
    private fun getPixelIndex(lng: Double, lat: Double, bounds: GeometryUtils.Bounds, width: Int, height: Int): Int {
        val x = ((lng - bounds.minLng) / (bounds.maxLng - bounds.minLng) * width).toInt().coerceIn(0, width - 1)
        val y = ((bounds.maxLat - lat) / (bounds.maxLat - bounds.minLat) * height).toInt().coerceIn(0, height - 1)
        return y * width + x
    }
}
