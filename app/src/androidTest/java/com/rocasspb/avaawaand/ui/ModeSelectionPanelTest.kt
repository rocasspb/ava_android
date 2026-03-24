package com.rocasspb.avaawaand.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rocasspb.avaawaand.BaseComposeTest
import com.rocasspb.avaawaand.MainViewModel
import com.rocasspb.avaawaand.ModeSelectionPanel
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import com.rocasspb.avaawaand.fakes.FakeMainRepository
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModeSelectionPanelTest : BaseComposeTest() {

    @Test
    fun testModeSelectionChangesMode() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository())
        
        setContentWithTheme {
            ModeSelectionPanel(
                viewModel = viewModel,
                onClose = {}
            )
        }
        
        onNodeWithText("Custom").performClick()
        
        // Assert that Custom controls appear
        // stringResource(R.string.elevation_range_m)
        onNodeWithText("Elevation Range (m)").assertExists()
        onNodeWithText("Min Steepness (degrees)").assertExists()
        onNodeWithTag("WindRose").assertExists()
    }
}
