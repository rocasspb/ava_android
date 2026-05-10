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
}
