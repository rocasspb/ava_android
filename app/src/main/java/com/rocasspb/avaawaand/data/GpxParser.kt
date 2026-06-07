package com.rocasspb.avaawaand.data

import com.rocasspb.avaawaand.utils.GeometryUtils
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.UUID

/**
 * Parses GPX files using XmlPullParser.
 */
class GpxParser {

    fun parse(inputStream: InputStream): List<GpxTrack> {
        val tracks = mutableListOf<GpxTrack>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var currentTrackName = ""
        val currentPoints = mutableListOf<GpxPoint>()
        var insideTrk = false
        var insideTrkpt = false
        var currentLat = 0.0
        var currentLon = 0.0
        var currentElevation: Double? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "trk" -> {
                            insideTrk = true
                            currentTrackName = ""
                            currentPoints.clear()
                        }
                        "name" -> {
                            if (insideTrk && !insideTrkpt) {
                                currentTrackName = parser.nextText()
                            }
                        }
                        "trkpt" -> {
                            insideTrkpt = true
                            currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull() ?: 0.0
                            currentLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull() ?: 0.0
                            currentElevation = null
                        }
                        "ele" -> {
                            if (insideTrkpt) {
                                currentElevation = parser.nextText().toDoubleOrNull()
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "trkpt" -> {
                            currentPoints.add(GpxPoint(currentLat, currentLon, currentElevation))
                            insideTrkpt = false
                        }
                        "trk" -> {
                            if (currentPoints.isNotEmpty()) {
                                tracks.add(buildTrack(currentTrackName, currentPoints))
                            }
                            insideTrk = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return tracks
    }

    private fun buildTrack(name: String, points: List<GpxPoint>): GpxTrack {
        var distance = 0.0
        var elevationGain = 0.0
        var elevationLoss = 0.0

        for (i in 0 until points.size - 1) {
            val p1 = points[i]
            val p2 = points[i + 1]

            distance += GeometryUtils.calculateDistance(p1.lat, p1.lon, p2.lat, p2.lon)

            val elev1 = p1.elevation
            val elev2 = p2.elevation
            if (elev1 != null && elev2 != null) {
                val diff = elev2 - elev1
                if (diff > 0) {
                    elevationGain += diff
                } else {
                    elevationLoss += Math.abs(diff)
                }
            }
        }

        return GpxTrack(
            id = UUID.randomUUID().toString(),
            name = if (name.isBlank()) "Imported Route" else name,
            points = points.toList(),
            distance = distance,
            elevationGain = elevationGain,
            elevationLoss = elevationLoss
        )
    }
}
