package com.rocasspb.avaawaand

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapbox.common.MapboxOptions
import com.rocasspb.avaawaand.fakes.FakeGpxRepository
import com.rocasspb.avaawaand.fakes.FakeMainRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaplessMainScreenTest : BaseComposeTest() {

    private val fakeRepository = FakeMainRepository()
    private val fakeGpxRepository = FakeGpxRepository()

    @Before
    fun setup() {
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    }

    @Test
    fun testMainScreenWithoutRealMap() {
        val viewModel = MainViewModel(fakeRepository, fakeGpxRepository)
        
        setContentWithTheme {
            MainScreen(viewModel, requestPermissions = false, mapContent = { _, _ -> 
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("Mock Map Content")
                }
            })
        }
        
        onNodeWithContentDescription("Select Mode").assertExists()
    }
}
