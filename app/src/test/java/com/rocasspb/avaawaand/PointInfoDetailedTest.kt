package com.rocasspb.avaawaand

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.geojson.Point
import com.rocasspb.avaawaand.data.*
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import com.rocasspb.avaawaand.logic.TerrainRgbElevationProvider
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PointInfoDetailedTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    class FakeElevationProvider(private val baseElevation: Int) : TerrainRgbElevationProvider() {
        override suspend fun prepare(bounds: GeometryUtils.Bounds, mapZoom: Double) {
            // Do nothing
        }

        override fun getElevation(point: GeometryUtils.Point): Int {
            // For aspect calculation in TerrainUtils:
            // zN = queryElevation(Point(lng, lat + 0.0001))
            // zS = queryElevation(Point(lng, lat - 0.0001))
            // Aspect North means zN < zS.
            return if (point.y > 47.26) baseElevation - 100 else baseElevation
        }
    }

    @Test
    fun `getPointInfo should include all danger levels and avalanche problems`() = runTest(testDispatcher) {
        val regionId = "AT-07-23-02"
        val testPoint = Point.fromLngLat(11.77, 47.26)
        
        // Mock data
        val regions = RegionResponse(
            type = "FeatureCollection",
            features = listOf(
                RegionFeature(
                    type = "Feature",
                    properties = RegionProperties(id = regionId, startDate = "2026-01-01", endDate = null),
                    geometry = Geometry(
                        type = "MultiPolygon", 
                        coordinates = listOf(listOf(listOf(
                            listOf(11.0, 47.0),
                            listOf(12.0, 47.0),
                            listOf(12.0, 48.0),
                            listOf(11.0, 48.0),
                            listOf(11.0, 47.0)
                        )))
                    )
                )
            )
        )

        val avalancheProblem = AvalancheProblem(
            problemType = "persistent_weak_layers",
            elevation = Elevation(lowerBound = "2000", upperBound = null),
            aspects = listOf("N", "NE", "NW"),
            validTimePeriod = "all_day",
            snowpackStability = "poor",
            frequency = "few",
            avalancheSize = 1
        )

        val bulletin = AvalancheData(
            bulletinID = "test-id",
            publicationTime = "2026-02-13T12:00:00Z",
            validTime = ValidTime("2026-02-13T12:00:00Z", "2026-02-14T12:00:00Z"),
            avalancheActivity = null,
            snowpackStructure = null,
            dangerRatings = listOf(
                DangerRating(mainValue = "moderate", validTimePeriod = "all_day", elevation = Elevation(null, "2000")),
                DangerRating(mainValue = "considerable", validTimePeriod = "all_day", elevation = Elevation("2000", null))
            ),
            avalancheProblems = listOf(avalancheProblem),
            tendency = null,
            weatherForecast = null,
            weatherReview = null,
            regions = listOf(Region(id = regionId, name = "Test Region"))
        )

        val fakeRepo = object : MainRepository {
            override suspend fun getRegions(): RegionResponse = regions
            override suspend fun getAvalancheData(): AvalancheResponse = AvalancheResponse(listOf(bulletin))
            override fun getPersistedRegions(): RegionResponse? = null
            override fun getPersistedAvalancheData(): AvalancheResponse? = null
            override fun persistRegions(regions: RegionResponse) {}
            override fun persistAvalancheData(avalanche: AvalancheResponse) {}
            override fun isFresh(avalancheData: AvalancheData): Boolean = true
            override fun isDisclaimerAccepted(): Boolean = true
            override fun setDisclaimerAccepted(accepted: Boolean) {}
        }

        val fakeElevationProvider = FakeElevationProvider(2500)
        val viewModel = MainViewModel(fakeRepo, FakeGpxRepository(), fakeElevationProvider, testDispatcher, testDispatcher)
        advanceUntilIdle() // Process fetchData

        // Act
        viewModel.getPointInfo(testPoint, 12.0)
        advanceUntilIdle()

        // Assert
        val info = viewModel.pointInfo.value
        assertNotNull("PointInfo should not be null", info)
        assertNotNull("Danger ratings should not be null", info?.dangerRatings)
        assertEquals(2, info?.dangerRatings?.size)
        // Check sorting (considerable > moderate)
        assertEquals("considerable", info?.dangerRatings?.get(0)?.mainValue)
        assertEquals("moderate", info?.dangerRatings?.get(1)?.mainValue)

        assertNotNull("Avalanche problems should not be null", info?.avalancheProblems)
        assertEquals(1, info?.avalancheProblems?.size)
        assertEquals("persistent_weak_layers", info?.avalancheProblems?.get(0)?.problemType)
    }
}
