package com.rocasspb.avaawaand

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.mapbox.maps.Style
import com.rocasspb.avaawaand.data.GpxRepository
import com.rocasspb.avaawaand.data.GpxTrack
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import com.rocasspb.avaawaand.logic.VisualizationMode
import com.rocasspb.avaawaand.data.AvalancheData
import com.rocasspb.avaawaand.data.AvalancheResponse
import com.rocasspb.avaawaand.data.Geometry
import com.rocasspb.avaawaand.data.MainRepository
import com.rocasspb.avaawaand.data.Region
import com.rocasspb.avaawaand.data.RegionFeature
import com.rocasspb.avaawaand.data.RegionProperties
import com.rocasspb.avaawaand.data.RegionResponse
import com.rocasspb.avaawaand.data.ValidTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ShadowLog.stream = System.out
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadMapConfig() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository(), ioDispatcher = testDispatcher, defaultDispatcher = testDispatcher)

        viewModel.loadMapConfig()

        val styleUrl = viewModel.mapStyleUrl.value

        assertNotNull(styleUrl)
        assertEquals(Style.OUTDOORS, styleUrl)

        val position = viewModel.initialCameraPosition.value
        assertNotNull(position)
    }

    @Test
    fun testUpdateCameraPosition() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository(), ioDispatcher = testDispatcher, defaultDispatcher = testDispatcher)
        val newPosition = com.mapbox.maps.CameraOptions.Builder()
            .center(com.mapbox.geojson.Point.fromLngLat(12.0, 48.0))
            .zoom(10.0)
            .build()

        viewModel.updateCameraPosition(newPosition)

        assertEquals(newPosition, viewModel.cameraPosition.value)
    }

    @Test
    fun testAutoSwitchingZoomIn() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository(), ioDispatcher = testDispatcher, defaultDispatcher = testDispatcher)
        viewModel.setVisualizationMode(VisualizationMode.BULLETIN)

        val newPosition = com.mapbox.maps.CameraOptions.Builder()
            .zoom(10.5)
            .build()
        viewModel.updateCameraPosition(newPosition)

        assertEquals(VisualizationMode.RISK, viewModel.visualizationMode.value)
    }

    @Test
    fun testAutoSwitchingZoomOut() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository(), ioDispatcher = testDispatcher, defaultDispatcher = testDispatcher)
        
        // Test RISK -> BULLETIN
        viewModel.setVisualizationMode(VisualizationMode.RISK)
        viewModel.updateCameraPosition(com.mapbox.maps.CameraOptions.Builder().zoom(9.5).build())
        assertEquals(VisualizationMode.BULLETIN, viewModel.visualizationMode.value)

        // Test CUSTOM -> BULLETIN
        viewModel.setVisualizationMode(VisualizationMode.CUSTOM)
        viewModel.updateCameraPosition(com.mapbox.maps.CameraOptions.Builder().zoom(9.0).build())
        assertEquals(VisualizationMode.BULLETIN, viewModel.visualizationMode.value)
    }

    @Test
    fun testOffModeIgnoresZoom() {
        val viewModel = MainViewModel(FakeMainRepository(), FakeGpxRepository(), ioDispatcher = testDispatcher, defaultDispatcher = testDispatcher)
        viewModel.setVisualizationMode(VisualizationMode.OFF)

        viewModel.updateCameraPosition(com.mapbox.maps.CameraOptions.Builder().zoom(15.0).build())
        assertEquals(VisualizationMode.OFF, viewModel.visualizationMode.value)

        viewModel.updateCameraPosition(com.mapbox.maps.CameraOptions.Builder().zoom(10.0).build())
        assertEquals(VisualizationMode.OFF, viewModel.visualizationMode.value)
    }

    @Test
    fun testFetchData() = runTest(testDispatcher) {
        val fakeRepo = FakeMainRepository()
        val viewModel = MainViewModel(fakeRepo, FakeGpxRepository(), ioDispatcher = testDispatcher, defaultDispatcher = testDispatcher)
        
        // Advance time to allow init block to run fetchData
        advanceUntilIdle()

        val regions = viewModel.regions.value
        assertNotNull("Regions should not be null", regions)
        assertEquals("FeatureCollection", regions?.type)

        val avalancheData = viewModel.avalancheData.value
        assertNotNull("Avalanche data should not be null", avalancheData)
    }

    @Test
    fun testParseRegions() {
        val gson = Gson()
        val json = """
            {"type":"FeatureCollection","features":[{"type":"Feature","properties":{"id":"AT-02-14","start_date":"2025-08-01","end_date":null},"geometry":{"type":"MultiPolygon","coordinates":[[[[12.7221099,46.7026896],[12.7210542,46.7027712]]]]}}]}
        """.trimIndent()

        val response = gson.fromJson(json, RegionResponse::class.java)

        assertNotNull(response)
        assertEquals("FeatureCollection", response.type)
        assertEquals(1, response.features.size)
        val feature = response.features[0]
        assertEquals("Feature", feature.type)
        assertEquals("AT-02-14", feature.properties.id)
        assertEquals("2025-08-01", feature.properties.startDate)
        assertEquals("MultiPolygon", feature.geometry.type)
        assertNotNull(feature.geometry.coordinates)
    }

    @Test
    fun testParseAvalancheData() {
        val gson = Gson()
        val json = """
            {"bulletins":[{"publicationTime":"2025-12-26T16:00:00Z","validTime":{"startTime":"2025-12-26T16:00:00Z","endTime":"2025-12-27T16:00:00Z"},"unscheduled":false,"avalancheActivity":{"highlights":"Wind slabs and weakly bonded old snow require caution.","comment":"Comment text."},"snowpackStructure":{"comment":"Snowpack comment."},"tendency":[{"highlights":"Low avalanche danger will prevail.","tendencyType":"steady","validTime":{"startTime":"2025-12-27T16:00:00Z","endTime":"2025-12-28T16:00:00Z"}}],"customData":{"ALBINA":{"mainDate":"2025-12-27"},"LWD_Tyrol":{"dangerPatterns":["DP1"]}},"avalancheProblems":[{"problemType":"persistent_weak_layers","elevation":{"lowerBound":"2600"},"validTimePeriod":"all_day","snowpackStability":"poor","frequency":"few","avalancheSize":1,"customData":{"ALBINA":{"avalancheType":"slab"}},"aspects":["NE","NW","N"]}],"bulletinID":"76470d99-791b-4910-b7c8-99adb6197969","dangerRatings":[{"mainValue":"low","validTimePeriod":"all_day"}],"lang":"en","regions":[{"name":"Zillertal Alps Northeast","regionID":"AT-07-23-02"}]}]}
        """.trimIndent()

        val response = gson.fromJson(json, AvalancheResponse::class.java)

        assertNotNull(response)
        assertEquals(1, response.bulletins.size)
        val bulletin = response.bulletins[0]
        assertEquals("76470d99-791b-4910-b7c8-99adb6197969", bulletin.bulletinID)
        assertEquals("2025-12-26T16:00:00Z", bulletin.publicationTime)
        assertEquals("2025-12-26T16:00:00Z", bulletin.validTime.startTime)
        assertNotNull(bulletin.avalancheActivity)
        assertEquals("Wind slabs and weakly bonded old snow require caution.", bulletin.avalancheActivity?.highlights)
        assertNotNull(bulletin.regions)
        assertEquals(1, bulletin.regions.size)
        assertEquals("AT-07-23-02", bulletin.regions[0].id)
    }
}

class FakeMainRepository : MainRepository {
    private var persistedRegions: RegionResponse? = null
    private var persistedAvalanche: AvalancheResponse? = null

    override suspend fun getRegions(): RegionResponse {
        return RegionResponse("FeatureCollection", listOf(
            RegionFeature("Feature", RegionProperties("test-id", "2024-01-01", null), 
                Geometry("MultiPolygon", emptyList()))
        ))
    }

    override suspend fun getAvalancheData(): AvalancheResponse {
        return AvalancheResponse(listOf(
            AvalancheData(
                bulletinID = "bulletin-id",
                publicationTime = "2024-01-01T00:00:00Z",
                validTime = ValidTime("2024-01-01T00:00:00Z", "2024-01-02T00:00:00Z"),
                avalancheActivity = null,
                snowpackStructure = null,
                dangerRatings = null,
                avalancheProblems = null,
                tendency = null,
                weatherForecast = null,
                weatherReview = null,
                regions = listOf(Region("test-id", "Test Region"))
            )
        ))
    }

    override fun getPersistedRegions(): RegionResponse? = persistedRegions

    override fun getPersistedAvalancheData(): AvalancheResponse? = persistedAvalanche

    override fun persistRegions(regions: RegionResponse) {
        persistedRegions = regions
    }

    override fun persistAvalancheData(avalanche: AvalancheResponse) {
        persistedAvalanche = avalanche
    }

    override fun isFresh(avalancheData: AvalancheData): Boolean = true
}
