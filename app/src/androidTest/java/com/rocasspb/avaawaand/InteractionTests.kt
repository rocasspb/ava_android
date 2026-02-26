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
        
        // Verifying the actual call to MapViewportState is hard because it's a final class
        // but we've verified the interaction path is enabled and clickable.
    }

    @Test
    fun testStyleToggleInteraction() {
        setMainScreenContent()
        
        // Initial style
        assert(viewModel.mapStyleUrl.value == com.mapbox.maps.Style.OUTDOORS)
        
        onNodeWithContentDescription("Switch Map Style").performClick()
        
        // Should switch to SATELLITE
        // Note: LiveData update might take a moment due to postValue
        composeTestRule.waitUntil {
            viewModel.mapStyleUrl.value == com.mapbox.maps.Style.SATELLITE
        }
    }
}
