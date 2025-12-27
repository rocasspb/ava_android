package com.rocasspb.avaawaand

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import com.rocasspb.avaawaand.data.AvalancheResponse
import com.rocasspb.avaawaand.data.MainRepository
import com.rocasspb.avaawaand.data.RegionResponse
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
import java.util.Collections

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

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

    @Test
    fun testLoadMapConfig() {
        val viewModel = MainViewModel(FakeMainRepository())

        viewModel.loadMapConfig()

        val styleUrl = viewModel.mapStyleUrl.value

        assertNotNull(styleUrl)

        assert(styleUrl!!.startsWith("https://api.maptiler.com/maps/winter-v2/style.json?key="))

        val expectedKey = BuildConfig.MAPTILER_KEY
        assertEquals("https://api.maptiler.com/maps/winter-v2/style.json?key=$expectedKey", styleUrl)

        val position = viewModel.initialCameraPosition.value
        assertNotNull(position)
    }

    @Test
    fun testFetchData() = runTest(testDispatcher) {
        val fakeRepo = FakeMainRepository()
        val viewModel = MainViewModel(fakeRepo)
        
        // Advance time to allow init block to run fetchData
        advanceUntilIdle()

        val regions = viewModel.regions.value
        assertNotNull(regions)
        assertEquals("FeatureCollection", regions?.type)

        val avalancheData = viewModel.avalancheData.value
        assertNotNull(avalancheData)
        // Fake repo returns empty lists by default or we can populate it
    }

    @Test
    fun testParseRegions() {
        val gson = Gson()
        // Simulating the JSON content from api_responses.txt for Regions
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
        // Simulating a snippet of the JSON content from api_responses.txt for Avalanche Data
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
    override suspend fun getRegions(): RegionResponse {
        return RegionResponse("FeatureCollection", Collections.emptyList())
    }

    override suspend fun getAvalancheData(): AvalancheResponse {
        return AvalancheResponse(Collections.emptyList())
    }
}
