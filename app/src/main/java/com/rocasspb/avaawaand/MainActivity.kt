package com.rocasspb.avaawaand

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mapbox.common.MapboxOptions
import com.rocasspb.avaawaand.logic.VisualizationMode
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN

        // Restore state
        val prefs = getPreferences(MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 47.26f).toDouble()
        val lon = prefs.getFloat("lon", 11.77f).toDouble()
        val zoom = prefs.getFloat("zoom", 8.0f).toDouble()
        val modeName = prefs.getString("mode", VisualizationMode.BULLETIN.name)
        val mode = try {
            VisualizationMode.valueOf(modeName ?: VisualizationMode.BULLETIN.name)
        } catch (_: Exception) {
            VisualizationMode.BULLETIN
        }

        viewModel.restoreState(lat, lon, zoom, mode)

        // Handle intent for opening GPX file
        handleGpxIntent(intent)

        setContent {
            AvaAwaAndTheme {
                MainScreen(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleGpxIntent(intent)
    }

    private fun handleGpxIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (intent.action == Intent.ACTION_VIEW) {
            try {
                contentResolver.openInputStream(data)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    viewModel.importGpx(bytes.inputStream())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val camera = viewModel.cameraPosition.value ?: return
        val target = camera.center ?: return
        val mode = viewModel.visualizationMode.value ?: VisualizationMode.OFF
        
        val prefs = getPreferences(MODE_PRIVATE)
        prefs.edit {
            putFloat("lat", target.latitude().toFloat())
            putFloat("lon", target.longitude().toFloat())
            putFloat("zoom", camera.zoom?.toFloat() ?: 8f)
            putString("mode", mode.name)
        }
    }
}

@Composable
fun AvaAwaAndTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF1A73E8),
            surface = Color.White,
            onSurface = Color.Black
        ),
        content = content
    )
}
