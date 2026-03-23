package com.rocasspb.avaawaand.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GpxParserTest {
    private val simpleGpx = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="AvaAwaAnd">
            <trk>
                <name>Test Route</name>
                <trkseg>
                    <trkpt lat="46.0" lon="10.0">
                        <ele>1500.0</ele>
                    </trkpt>
                    <trkpt lat="46.01" lon="10.01">
                        <ele>1600.0</ele>
                    </trkpt>
                </trkseg>
            </trk>
        </gpx>
    """.trimIndent()

    @Test
    fun testParseSimpleGpx() {
        val parser = GpxParser()
        val inputStream = ByteArrayInputStream(simpleGpx.toByteArray())
        val tracks = parser.parse(inputStream)

        assertEquals(1, tracks.size)
        val track = tracks[0]
        assertEquals("Test Route", track.name)
        assertEquals(2, track.points.size)
        assertEquals(46.0, track.points[0].lat, 0.0001)
        assertEquals(10.0, track.points[0].lon, 0.0001)
        assertEquals(1500.0, track.points[0].elevation!!, 0.0001)
        assertEquals(46.01, track.points[1].lat, 0.0001)
        assertEquals(10.01, track.points[1].lon, 0.0001)
        assertEquals(1600.0, track.points[1].elevation!!, 0.0001)
    }

    @Test
    fun testGpxCalculations() {
        val parser = GpxParser()
        val inputStream = ByteArrayInputStream(simpleGpx.toByteArray())
        val tracks = parser.parse(inputStream)

        assertEquals(1, tracks.size)
        val track = tracks[0]
        
        // Approx distance between (46.0, 10.0) and (46.01, 10.01) is ~1356m
        assertEquals(1356.0, track.distance, 10.0)
        assertEquals(100.0, track.elevationGain, 0.1)
        assertEquals(0.0, track.elevationLoss, 0.1)
    }
}
