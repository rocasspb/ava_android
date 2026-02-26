package com.rocasspb.avaawaand.ui

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rocasspb.avaawaand.BaseComposeTest
import com.rocasspb.avaawaand.WindRose
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WindRoseTest : BaseComposeTest() {

    @Test
    fun testWindRoseClickCallsCallback() {
        var clickedAspect: String? = null
        
        setContentWithTheme {
            WindRose(
                selectedAspects = setOf(),
                onAspectClick = { clickedAspect = it }
            )
        }
        
        onNodeWithTag("WindRose").performTouchInput {
            click(center + androidx.compose.ui.geometry.Offset(width * 0.4f, 0f))
        }
        
        assert(clickedAspect == "E")
    }
}
