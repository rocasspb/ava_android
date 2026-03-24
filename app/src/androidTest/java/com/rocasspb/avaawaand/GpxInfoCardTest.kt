package com.rocasspb.avaawaand

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rocasspb.avaawaand.data.GpxPoint
import com.rocasspb.avaawaand.data.GpxTrack
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GpxInfoCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testGpxInfoCardDisplaysCorrectMetadata() {
        val track = GpxTrack(
            id = "test-track",
            name = "Test Track",
            points = listOf(GpxPoint(48.0, 12.0, 0.0)),
            distance = 1200.0,
            elevationGain = 300.0,
            elevationLoss = 100.0
        )

        composeTestRule.setContent {
            GpxInfoCard(
                track = track,
                onDeleteClick = {}
            )
        }

        onNodeWithText("Test Track").assertExists()
        onNodeWithText("1.2 km").assertExists()
        onNodeWithText("300m").assertExists()
        onNodeWithText("100m").assertExists()
    }

    @Test
    fun testGpxInfoCardDeleteClick() {
        var deleteClicked = false
        val track = GpxTrack(
            id = "test-track",
            name = "Test Track",
            points = listOf(GpxPoint(48.0, 12.0, 0.0)),
            distance = 0.0,
            elevationGain = 0.0,
            elevationLoss = 0.0
        )

        composeTestRule.setContent {
            GpxInfoCard(
                track = track,
                onDeleteClick = { deleteClicked = true }
            )
        }

        onNodeWithContentDescription("Delete Route").performClick()
        assert(deleteClicked)
    }
}
