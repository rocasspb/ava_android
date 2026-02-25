package com.rocasspb.avaawaand

import org.junit.Assert.assertEquals
import org.junit.Test

class WindRoseLogicTest {

    @Test
    fun `test angle to sector mapping`() {
        // Test East (0)
        assertEquals("E", findSectorForAngle(0f)?.id)
        assertEquals("E", findSectorForAngle(10f)?.id)
        assertEquals("E", findSectorForAngle(-10f)?.id)
        assertEquals("E", findSectorForAngle(350f)?.id)

        // Test South (90)
        assertEquals("S", findSectorForAngle(90f)?.id)
        assertEquals("S", findSectorForAngle(80f)?.id)
        assertEquals("S", findSectorForAngle(100f)?.id)

        // Test West (180)
        assertEquals("W", findSectorForAngle(180f)?.id)
        assertEquals("W", findSectorForAngle(170f)?.id)
        assertEquals("W", findSectorForAngle(190f)?.id)

        // Test North (270)
        assertEquals("N", findSectorForAngle(270f)?.id)
        assertEquals("N", findSectorForAngle(260f)?.id)
        assertEquals("N", findSectorForAngle(280f)?.id)

        // Test Intercardinals
        assertEquals("SE", findSectorForAngle(45f)?.id)
        assertEquals("SW", findSectorForAngle(135f)?.id)
        assertEquals("NW", findSectorForAngle(225f)?.id)
        assertEquals("NE", findSectorForAngle(315f)?.id)
    }

    @Test
    fun `test angle boundaries`() {
        // East boundary (0 +/- 22.5)
        assertEquals("E", findSectorForAngle(22.4f)?.id)
        assertEquals("SE", findSectorForAngle(22.6f)?.id)
        assertEquals("E", findSectorForAngle(-22.4f)?.id)
        assertEquals("NE", findSectorForAngle(-22.6f)?.id)

        // North boundary (270 +/- 22.5)
        assertEquals("N", findSectorForAngle(247.6f)?.id)
        assertEquals("NW", findSectorForAngle(247.4f)?.id)
        assertEquals("N", findSectorForAngle(292.4f)?.id)
        assertEquals("NE", findSectorForAngle(292.6f)?.id)
    }
}
