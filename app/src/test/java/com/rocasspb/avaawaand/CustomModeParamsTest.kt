package com.rocasspb.avaawaand

import com.rocasspb.avaawaand.logic.CustomModeParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomModeParamsTest {

    @Test
    fun `test updating aspects in CustomModeParams`() {
        var params = CustomModeParams()
        
        // Initial state
        assertEquals(8, params.aspects.size)

        // Clear all aspects
        params = params.copy(aspects = emptyList())
        assertTrue(params.aspects.isEmpty())

        // Add an aspect
        val aspect1 = "N"
        params = params.copy(aspects = params.aspects + aspect1)
        assertTrue(params.aspects.contains(aspect1))
        assertEquals(1, params.aspects.size)

        // Add another
        val aspect2 = "NE"
        params = params.copy(aspects = params.aspects + aspect2)
        assertTrue(params.aspects.contains(aspect2))
        assertEquals(2, params.aspects.size)

        // Remove one
        params = params.copy(aspects = params.aspects.filter { it != aspect1 })
        assertFalse(params.aspects.contains(aspect1))
        assertTrue(params.aspects.contains(aspect2))
        assertEquals(1, params.aspects.size)
    }
}
