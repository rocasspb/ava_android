package com.rocasspb.avaawaand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import com.rocasspb.avaawaand.fakes.FakeMainRepository
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest : BaseComposeTest() {

    @Test
    fun testModeSelectionPanelOpensOnButtonClick() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository())
        
        setContentWithTheme {
            MainScreen(viewModel, requestPermissions = false, mapContent = { _, _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Mock Map")
                }
            })
        }
        
        onNodeWithContentDescription("Select Mode").performClick()
        
        // Assert that the panel is now displayed
        onNodeWithText("Bulletin").assertExists()
    }

    @Test
    fun testGpxFabExists() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository())
        
        setContentWithTheme {
            MainScreen(viewModel, requestPermissions = false, mapContent = { _, _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Mock Map")
                }
            })
        }
        
        onNodeWithContentDescription("Import GPX").assertExists()
    }
}
