package com.rocasspb.avaawaand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rocasspb.avaawaand.fakes.FakeMainRepository
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeTest : BaseComposeTest() {

    @Test
    fun testTypicalUserFlow() {
        val viewModel = MainViewModel(FakeMainRepository())
        
        setContentWithTheme {
            MainScreen(viewModel, requestPermissions = false, mapContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Mock Map")
                }
            })
        }
        
        // 1. Open Mode Selection
        onNodeWithContentDescription("Select Mode").performClick()
        
        // 2. Select Custom mode
        onNodeWithText("Custom").performClick()
        
        // 3. Verify Custom controls appear
        onNodeWithTag("WindRose").assertExists()
        
        // 4. Click an aspect in WindRose
        onNodeWithTag("WindRose").performTouchInput {
            click(center + androidx.compose.ui.geometry.Offset(width * 0.4f, 0f))
        }
        
        // 5. Close panel
        onNodeWithContentDescription("Close").performClick()
        
        // 6. Verify panel is closed (e.g. by checking if button is visible again)
        onNodeWithContentDescription("Select Mode").assertExists()
    }
}
