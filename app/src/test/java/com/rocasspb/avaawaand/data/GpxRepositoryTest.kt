package com.rocasspb.avaawaand.data

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GpxRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: GpxRepository

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        repository = GpxRepositoryImpl(context)
    }

    @Test
    fun testSaveAndLoadTrack() {
        val track = GpxTrack(
            id = "test-id",
            name = "Test Track",
            points = listOf(GpxPoint(46.0, 10.0, 1500.0)),
            distance = 0.0,
            elevationGain = 0.0,
            elevationLoss = 0.0
        )

        repository.saveTrack(track)
        val loadedTracks = repository.getAllTracks()

        assertEquals(1, loadedTracks.size)
        assertEquals("test-id", loadedTracks[0].id)
        assertEquals("Test Track", loadedTracks[0].name)
    }

    @Test
    fun testDeleteTrack() {
        val track = GpxTrack(
            id = "test-id",
            name = "Test Track",
            points = listOf(GpxPoint(46.0, 10.0, 1500.0)),
            distance = 0.0,
            elevationGain = 0.0,
            elevationLoss = 0.0
        )

        repository.saveTrack(track)
        repository.deleteTrack("test-id")
        val loadedTracks = repository.getAllTracks()

        assertTrue(loadedTracks.isEmpty())
    }
}
