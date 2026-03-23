package com.rocasspb.avaawaand

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.rocasspb.avaawaand.data.GpxTrack

@Composable
fun GpxOverlay(
    gpxTracks: List<GpxTrack>,
    selectedTrack: GpxTrack?,
    onTrackClick: (GpxTrack) -> Unit
) {
    gpxTracks.forEach { track ->
        val points = track.points.map { Point.fromLngLat(it.lon, it.lat) }
        if (points.size >= 2) {
            val isSelected = track.id == selectedTrack?.id
            PolylineAnnotation(
                points = points,
                onClick = {
                    onTrackClick(track)
                    true
                }
            ) {
                lineColor = if (isSelected) Color.Red else Color(0xFF1A73E8)
                lineWidth = if (isSelected) 5.0 else 3.0
            }
        }
    }
}
