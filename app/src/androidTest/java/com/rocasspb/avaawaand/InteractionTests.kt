package com.rocasspb.avaawaand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.rocasspb.avaawaand.fakes.FakeMainRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InteractionTests : BaseComposeTest() {

    private lateinit var viewModel: MainViewModel
    private var capturedViewportState: MapViewportState? = null

    @Before
    fun setup() {
        viewModel = MainViewModel(FakeMainRepository())
    }

    private fun setMainScreenContent() {
        setContentWithTheme {
            MainScreen(
                viewModel = viewModel,
                requestPermissions = false,
                mapContent = { viewportState ->
                    capturedViewportState = viewportState
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("Mock Map")
                    }
                }
            )
        }
    }

    @Test
    fun testLocationFabInteraction() {
        // Initially FAB should not exist if permission not granted
        viewModel.setLocationPermissionGranted(false)
        setMainScreenContent()
        onNodeWithContentDescription("My Location").assertDoesNotExist()

        // Grant permission and it should appear
        viewModel.setLocationPermissionGranted(true)
        // Wait for UI to update
        onNodeWithContentDescription("My Location").assertExists().performClick()
    }

    @Test
    fun testPitchToggleInteraction() {
        setMainScreenContent()
        
        // Initially it should show "3D" (because initial pitch is 0.0)
        onNodeWithText("3D").assertExists().performClick()
    }

    @Test
    fun testStyleToggleInteraction() {
        setMainScreenContent()
        
        // Initial style
        assert(viewModel.mapStyleUrl.value == com.mapbox.maps.Style.OUTDOORS)
        
        onNodeWithContentDescription("Switch Map Style").performClick()
        
        // Should switch to SATELLITE
        composeTestRule.waitUntil {
            viewModel.mapStyleUrl.value == com.mapbox.maps.Style.SATELLITE
        }
    }

    @Test
    fun testModeSwitching() {
        setMainScreenContent()
        
        // Open panel
        onNodeWithContentDescription("Select Mode").performClick()
        
        // Click Risk mode
        onNodeWithText("Risk").performClick()
        composeTestRule.waitUntil {
            viewModel.visualizationMode.value == com.rocasspb.avaawaand.logic.VisualizationMode.RISK
        }
        
        // Click Custom mode
        onNodeWithText("Custom").performClick()
        composeTestRule.waitUntil {
            viewModel.visualizationMode.value == com.rocasspb.avaawaand.logic.VisualizationMode.CUSTOM
        }
        
        // Click Off mode
        onNodeWithText("Off").performClick()
        composeTestRule.waitUntil {
            viewModel.visualizationMode.value == com.rocasspb.avaawaand.logic.VisualizationMode.OFF
        }
    }

    @Test
    fun testModeUIState() {
        setMainScreenContent()
        
        // Open panel
        onNodeWithContentDescription("Select Mode").performClick()
        
        // Initially Bulletin should be selected
        onNode(hasText("Bulletin") and isSelected()).assertExists()
        onNode(hasText("Risk") and isSelected()).assertDoesNotExist()
        
        // Click Risk mode
        onNodeWithText("Risk").performClick()
        
        // Now Risk should be selected
        onNode(hasText("Risk") and isSelected()).assertExists()
        onNode(hasText("Bulletin") and isSelected()).assertDoesNotExist()
    }

    @Test
    fun testPanelVisibility() {
        setMainScreenContent()
        
        // Panel should be hidden initially
        onNodeWithText("Bulletin").assertDoesNotExist()
        
        // Open panel
        onNodeWithContentDescription("Select Mode").assertExists().performClick()
        onNodeWithText("Bulletin").assertExists()
        
        // Close panel
        onNodeWithContentDescription("Close").assertExists().performClick()
        
        // Panel should be hidden again
        onNodeWithText("Bulletin").assertDoesNotExist()
    }
}
