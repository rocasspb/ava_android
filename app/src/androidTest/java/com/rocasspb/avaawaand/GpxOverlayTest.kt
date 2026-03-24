package com.rocasspb.avaawaand

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.rocasspb.avaawaand.data.GpxPoint
import com.rocasspb.avaawaand.data.GpxTrack
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GpxOverlayTest : BaseComposeTest() {

    @Test
    fun testGpxOverlayRendersWithoutCrash() {
        val tracks = listOf(
            GpxTrack(
                id = "test-track",
                name = "Test Track",
                points = listOf(
                    GpxPoint(48.0, 12.0, 1000.0),
                    GpxPoint(48.1, 12.1, 1100.0)
                ),
                distance = 1000.0,
                elevationGain = 100.0,
                elevationLoss = 0.0
            )
        )

        setContentWithTheme {
            val mapViewportState = rememberMapViewportState()
            MapboxMap(
                mapViewportState = mapViewportState
            ) {
                GpxOverlay(
                    gpxTracks = tracks,
                    selectedTrack = null,
                    onTrackClick = {}
                )
            }
        }
        
        // If we reach here, it means the composition didn't crash
    }
}
