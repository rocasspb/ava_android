package com.rocasspb.avaawaand.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GpxDataTest {
    @Test
    fun testGpxPointCreation() {
        val point = GpxPoint(46.0, 10.0, 1500.0)
        assertEquals(46.0, point.lat, 0.0001)
        assertEquals(10.0, point.lon, 0.0001)
        assertEquals(1500.0, point.elevation!!, 0.0001)
    }

    @Test
    fun testGpxTrackCreation() {
        val points = listOf(
            GpxPoint(46.0, 10.0, 1500.0),
            GpxPoint(46.1, 10.1, 1600.0)
        )
        val track = GpxTrack(
            id = "test-id",
            name = "Test Track",
            points = points,
            distance = 15000.0,
            elevationGain = 100.0,
            elevationLoss = 0.0
        )
        assertEquals("test-id", track.id)
        assertEquals("Test Track", track.name)
        assertEquals(2, track.points.size)
        assertEquals(15000.0, track.distance, 0.1)
        assertEquals(100.0, track.elevationGain, 0.1)
        assertEquals(0.0, track.elevationLoss, 0.1)
    }
}
