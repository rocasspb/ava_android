package com.rocasspb.avaawaand.logic

import kotlin.test.Test
import kotlin.test.assertEquals

class OffscreenRendererTest {
    @Test
    fun testDrawPolygonBitmask() {
        val renderer = KorimRenderer()
        renderer.init(10, 10)
        
        // Draw a square from (2,2) to (5,5) with bitmask 1
        renderer.drawPolygon(listOf(2f to 2f, 5f to 2f, 5f to 5f, 2f to 5f), 1)
        
        // Draw a square from (4,4) to (7,7) with bitmask 2
        renderer.drawPolygon(listOf(4f to 4f, 7f to 4f, 7f to 7f, 4f to 7f), 2)
        
        val pixels = renderer.getPixels()
        
        // Check a point only in the first square
        assertEquals(1, pixels[3 * 10 + 3], "Point (3,3) should have bitmask 1")
        
        // Check a point in the overlap
        assertEquals(3, pixels[4 * 10 + 4], "Point (4,4) should have bitmask 3 (1 | 2)")
        
        // Check a point only in the second square
        assertEquals(2, pixels[6 * 10 + 6], "Point (6,6) should have bitmask 2")
        
        // Check a point outside both
        assertEquals(0, pixels[0 * 10 + 0], "Point (0,0) should have bitmask 0")
    }

    @Test
    fun testDrawTriangle() {
        val renderer = KorimRenderer()
        renderer.init(10, 10)
        
        // Triangle: (1,1), (8,1), (4,8)
        renderer.drawPolygon(listOf(1f to 1f, 8f to 1f, 4f to 8f), 4)
        
        val pixels = renderer.getPixels()
        
        assertEquals(4, pixels[2 * 10 + 4], "Point (4,2) should be inside triangle")
        assertEquals(0, pixels[9 * 10 + 9], "Point (9,9) should be outside triangle")
    }
}
