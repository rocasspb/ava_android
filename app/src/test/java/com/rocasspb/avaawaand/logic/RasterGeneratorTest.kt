package com.rocasspb.avaawaand.logic

import android.graphics.Bitmap
import android.graphics.Color
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RasterGeneratorTest {

    private class ConstantElevationProvider(val elevation: Double) : ElevationProvider {
        override fun getElevation(point: GeometryUtils.Point): Double = elevation
    }

    @Test
    fun `drawToBitmap returns null for invalid bounds`() {
        val bounds = GeometryUtils.Bounds(10.0, 10.0, 45.0, 45.0)
        val bitmap = RasterGenerator.drawToBitmap(emptyList(), bounds, ConstantElevationProvider(1000.0))
        assertNull(bitmap)
    }

    @Test
    fun `drawToBitmap generates bitmap with correct color for simple rule`() {
        val bounds = GeometryUtils.Bounds(10.0, 11.0, 45.0, 46.0)
        val ruleBounds = GeometryUtils.Bounds(10.0, 11.0, 45.0, 46.0)
        
        val rule = GenerationRule(
            bounds = ruleBounds,
            geometry = null,
            minElev = 500.0,
            maxElev = 2000.0,
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "3")
        )
        
        val bitmap = RasterGenerator.drawToBitmap(
            listOf(rule),
            bounds,
            ConstantElevationProvider(1000.0)
        )
        
        assertNotNull(bitmap)
        val pixel = bitmap!!.getPixel(bitmap.width / 2, bitmap.height / 2)
        assertEquals(Color.RED, pixel)
    }

    @Test
    fun `drawToBitmap filters by elevation`() {
        val bounds = GeometryUtils.Bounds(10.0, 11.0, 45.0, 46.0)
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 2000.0,
            maxElev = 3000.0,
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "3")
        )
        
        // Elevation 1000 is below minElev 2000
        val bitmap = RasterGenerator.drawToBitmap(
            listOf(rule),
            bounds,
            ConstantElevationProvider(1000.0)
        )
        
        assertNotNull(bitmap)
        val pixel = bitmap!!.getPixel(bitmap.width / 2, bitmap.height / 2)
        assertEquals(Color.TRANSPARENT, pixel)
    }

    @Test
    fun `drawToBitmap applies aspect filtering`() {
        val bounds = GeometryUtils.Bounds(11.7, 11.8, 47.2, 47.3)
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 0.0,
            maxElev = 4000.0,
            validAspects = listOf("N"),
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "low") // Danger 1 -> skip if wrong aspect
        )
        
        // Elevation provider that returns a north-facing slope
        val northFacingProvider = object : ElevationProvider {
            override fun getElevation(point: GeometryUtils.Point): Double {
                // North is +lat. If elevation decreases as we go North, aspect is North.
                // TerrainUtils uses offset 0.0001
                return 1000.0 - (point.y - 47.25) * 1000.0
            }
        }
        
        val bitmap = RasterGenerator.drawToBitmap(listOf(rule), bounds, northFacingProvider)
        assertNotNull(bitmap)
        val pixel = bitmap!!.getPixel(bitmap.width / 2, bitmap.height / 2)
        assertEquals(Color.RED, pixel)
        
        // Now test with wrong aspect for Danger 1
        val southFacingRule = rule.copy(validAspects = listOf("S"))
        val bitmap2 = RasterGenerator.drawToBitmap(listOf(southFacingRule), bounds, northFacingProvider)
        assertEquals(Color.TRANSPARENT, bitmap2!!.getPixel(bitmap2.width / 2, bitmap2.height / 2))
    }

    @Test
    fun `drawToBitmap applies steepness logic`() {
        val bounds = GeometryUtils.Bounds(11.7, 11.8, 47.2, 47.3)
        
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 0.0,
            maxElev = 4000.0,
            applySteepnessLogic = true,
            color = "#FF9900", // Orange for Considerable
            properties = RuleProperties(dangerLevel = "considerable")
        )

        val steepProvider = object : ElevationProvider {
            override fun getElevation(point: GeometryUtils.Point): Double {
                // zN - zS = 0.0002 * k
                // We want slope >= 35. tan(35) * 22.22 = 15.55
                // 0.0002 * 80000 = 16.0
                return 1000.0 + (point.y - 47.25) * 80000.0
            }
        }

        val bitmap = RasterGenerator.drawToBitmap(listOf(rule), bounds, steepProvider)
        assertNotNull(bitmap)
        
        val pixel = bitmap!!.getPixel(bitmap.width / 2, bitmap.height / 2)
        
        // Considerable is 3. 
        // if (effectiveDlValue == 3) { if (slope >= 35) finalColor = highColor ... }
        val highColor = Color.parseColor(AvalancheConfig.DANGER_COLORS[4])
        assertEquals("Should be red for slope >= 35 with danger 3", highColor, pixel)
    }
}
