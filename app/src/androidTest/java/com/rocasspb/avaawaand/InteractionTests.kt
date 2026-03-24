package com.rocasspb.avaawaand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import com.rocasspb.avaawaand.fakes.FakeMainRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InteractionTests : BaseComposeTest() {

    private lateinit var viewModel: MainViewModel
    private var capturedViewportState: MapViewportState? = null
    private var capturedLoadingCallback: ((Boolean) -> Unit)? = null

    @Before
    fun setup() {
        viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository())
    }

    private fun setMainScreenContent() {
        setContentWithTheme {
            MainScreen(
                viewModel = viewModel,
                requestPermissions = false,
                mapContent = { viewportState, loadingCallback ->
                    capturedViewportState = viewportState
                    capturedLoadingCallback = loadingCallback
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("Mock Map")
                    }
                }
            )
        }
    }

    @Test
    fun testLocationFabInteraction() {
        viewModel.setLocationPermissionGranted(false)
        setMainScreenContent()
        onNodeWithContentDescription("My Location").assertDoesNotExist()

        viewModel.setLocationPermissionGranted(true)
        onNodeWithContentDescription("My Location").assertExists().performClick()
    }

    @Test
    fun testPitchToggleInteraction() {
        setMainScreenContent()
        onNodeWithText("3D").assertExists().performClick()
    }

    @Test
    fun testStyleToggleInteraction() {
        setMainScreenContent()
        assert(viewModel.mapStyleUrl.value == com.mapbox.maps.Style.OUTDOORS)
        onNodeWithContentDescription("Switch Map Style").performClick()
        composeTestRule.waitUntil {
            viewModel.mapStyleUrl.value == com.mapbox.maps.Style.SATELLITE
        }
    }

    @Test
    fun testModeSwitching() {
        setMainScreenContent()
        onNodeWithContentDescription("Select Mode").performClick()
        
        onNodeWithText("Risk").performClick()
        composeTestRule.waitUntil {
            viewModel.visualizationMode.value == com.rocasspb.avaawaand.logic.VisualizationMode.RISK
        }
        
        onNodeWithText("Custom").performClick()
        composeTestRule.waitUntil {
            viewModel.visualizationMode.value == com.rocasspb.avaawaand.logic.VisualizationMode.CUSTOM
        }
        
        onNodeWithText("Off").performClick()
        composeTestRule.waitUntil {
            viewModel.visualizationMode.value == com.rocasspb.avaawaand.logic.VisualizationMode.OFF
        }
    }

    @Test
    fun testModeUIState() {
        setMainScreenContent()
        onNodeWithContentDescription("Select Mode").performClick()
        
        onNode(hasText("Bulletin") and isSelected()).assertExists()
        onNode(hasText("Risk") and isSelected()).assertDoesNotExist()
        
        onNodeWithText("Risk").performClick()
        onNode(hasText("Risk") and isSelected()).assertExists()
        onNode(hasText("Bulletin") and isSelected()).assertDoesNotExist()
    }

    @Test
    fun testPanelVisibility() {
        setMainScreenContent()
        onNodeWithText("Bulletin").assertDoesNotExist()
        
        onNodeWithContentDescription("Select Mode").assertExists().performClick()
        onNodeWithText("Bulletin").assertExists()
        
        onNodeWithContentDescription("Close").assertExists().performClick()
        onNodeWithText("Bulletin").assertDoesNotExist()
    }

    @Test
    fun testCustomModeSliders() {
        setMainScreenContent()
        onNodeWithContentDescription("Select Mode").performClick()
        onNodeWithText("Custom").performClick()
        
        onNodeWithText("1000m - 4000m").assertExists()
        onNodeWithText("30°").assertExists()
        
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(30f, 30f..45f, 14))).performTouchInput {
            swipeRight()
        }
        
        composeTestRule.waitUntil {
            (viewModel.customModeParams.value?.minSlope ?: 0) > 30
        }
        
        val newSlope = viewModel.customModeParams.value?.minSlope
        onNodeWithText("${newSlope}°").assertExists()
    }

    @Test
    fun testCustomAspectSelection() {
        setMainScreenContent()
        onNodeWithContentDescription("Select Mode").performClick()
        onNodeWithText("Custom").performClick()
        
        assert(viewModel.customModeParams.value?.aspects?.size == 8)
        
        onNodeWithTag("WindRose").performTouchInput {
            click(center + androidx.compose.ui.geometry.Offset(width * 0.4f, 0f))
        }
        
        composeTestRule.waitUntil {
            viewModel.customModeParams.value?.aspects?.size == 7
        }
        assert(viewModel.customModeParams.value?.aspects?.contains("E") == false)
    }

    @Test
    fun testLogicTriggering() {
        setMainScreenContent()
        
        // Manually trigger loading state via captured callback
        composeTestRule.runOnUiThread {
            capturedLoadingCallback?.invoke(true)
        }
        
        // Verify loading indicator appears
        onNodeWithTag("OverlayLoadingIndicator").assertExists()
        
        // Stop loading
        composeTestRule.runOnUiThread {
            capturedLoadingCallback?.invoke(false)
        }
        
        // Verify it disappears
        onNodeWithTag("OverlayLoadingIndicator").assertDoesNotExist()
    }
}
