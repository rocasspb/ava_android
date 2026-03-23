package com.rocasspb.avaawaand

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rocasspb.avaawaand.data.GpxPoint
import com.rocasspb.avaawaand.data.GpxRepository
import com.rocasspb.avaawaand.data.GpxTrack
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainViewModelGpxTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var gpxRepository: FakeGpxRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        gpxRepository = FakeGpxRepository()
        viewModel = MainViewModel(
            repository = FakeMainRepository(),
            gpxRepository = gpxRepository,
            ioDispatcher = testDispatcher,
            defaultDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadedGpxTracksInitiallyLoaded() = runTest(testDispatcher) {
        val track = GpxTrack("id", "name", listOf(GpxPoint(0.0, 0.0)), 0.0, 0.0, 0.0)
        gpxRepository.saveTrack(track)
        
        // Re-init viewModel to trigger loading
        viewModel = MainViewModel(
            repository = FakeMainRepository(),
            gpxRepository = gpxRepository,
            ioDispatcher = testDispatcher,
            defaultDispatcher = testDispatcher
        )
        
        advanceUntilIdle()
        
        val tracks = viewModel.gpxTracks.value
        assertEquals(1, tracks?.size)
        assertEquals("id", tracks?.get(0)?.id)
    }

    @Test
    fun testSelectedGpxTrack() {
        val track = GpxTrack("id", "name", emptyList(), 0.0, 0.0, 0.0)
        viewModel.selectGpxTrack(track)
        assertEquals(track, viewModel.selectedGpxTrack.value)
    }

    @Test
    fun testDeselectGpxTrack() {
        val track = GpxTrack("id", "name", emptyList(), 0.0, 0.0, 0.0)
        viewModel.selectGpxTrack(track)
        viewModel.deselectGpxTrack()
        assertEquals(null, viewModel.selectedGpxTrack.value)
    }
}
