package com.rocasspb.avaawaand.logic

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

    private class ConstantElevationProvider(val elevation: Int) : ElevationProvider {
        override fun getElevation(point: GeometryUtils.Point): Int = elevation
    }

    @Test
    fun `drawToBitmap returns null for invalid bounds`() {
        val bounds = GeometryUtils.Bounds(10.0, 10.0, 45.0, 45.0)
        val bitmap = RasterGenerator.drawToBitmap(emptyList(), bounds, ConstantElevationProvider(1000))
        assertNull(bitmap)
    }

    @Test
    fun `drawToBitmap generates bitmap with correct color for simple rule`() {
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
        
        val bitmap = RasterGenerator.drawToBitmap(
            listOf(rule),
            bounds,
            ConstantElevationProvider(1000)
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
            minElev = 2000,
            maxElev = 3000,
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "3")
        )
        
        // Elevation 1000 is below minElev 2000
        val bitmap = RasterGenerator.drawToBitmap(
            listOf(rule),
            bounds,
            ConstantElevationProvider(1000)
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
            minElev = 0,
            maxElev = 4000,
            validAspects = listOf("N"),
            color = "#FF0000",
            properties = RuleProperties(dangerLevel = "low") // Danger 1 -> skip if wrong aspect
        )
        
        // Elevation provider that returns a north-facing slope
        val northFacingProvider = object : ElevationProvider {
            override fun getElevation(point: GeometryUtils.Point): Int {
                // TerrainUtils uses offset 0.0001
                // Aspect calculation involves elevations at (p.x, p.y + offset) and (p.x, p.y - offset)
                // If elevation decreases as we go North (+y), the aspect is North.
                // dz_dy = (elev(N) - elev(S)) / (2 * offset)
                // To have aspect North, we need dz_dy < 0.
                return (1000 - (point.y - 47.25) * 100000).toInt()
            }
        }
        
        val bitmap = RasterGenerator.drawToBitmap(listOf(rule), bounds, northFacingProvider)
        assertNotNull(bitmap)
        val pixel = bitmap!!.getPixel(bitmap.width / 2, bitmap.height / 2)
        assertEquals("Should be Red on North aspect", Color.RED, pixel)
        
        // Now test with wrong aspect for Danger 1
        val southFacingRule = rule.copy(validAspects = listOf("S"))
        val bitmap2 = RasterGenerator.drawToBitmap(listOf(southFacingRule), bounds, northFacingProvider)
        assertEquals("Should be Transparent on wrong aspect", Color.TRANSPARENT, bitmap2!!.getPixel(bitmap2.width / 2, bitmap2.height / 2))
    }

    @Test
    fun `drawToBitmap applies steepness logic`() {
        val bounds = GeometryUtils.Bounds(11.7, 11.8, 47.2, 47.3)
        
        val rule = GenerationRule(
            bounds = bounds,
            geometry = null,
            minElev = 0,
            maxElev = 4000,
            applySteepnessLogic = true,
            color = "#FF9900", // Orange for Considerable
            properties = RuleProperties(dangerLevel = "considerable")
        )

        val steepProvider = object : ElevationProvider {
            override fun getElevation(point: GeometryUtils.Point): Int {
                // slope = atan(sqrt(dz_dx^2 + dz_dy^2)) * (180/PI)
                // dz_dy = (elev(N) - elev(S)) / (2 * offset)
                // offset = 0.0001. 2 * offset * 111120 = 22.224 meters.
                // We want slope >= 35. tan(35) * 22.224 = 15.56 meters difference.
                // dz_dy = 15.56 / 22.224 = 0.7
                // elev(N) - elev(S) = 15.56
                // elev = 1000 + y * (15.56 / (2 * 0.0001)) = 1000 + y * 77800
                return (1000.0 + (point.y - 47.25) * 80000.0).toInt()
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
