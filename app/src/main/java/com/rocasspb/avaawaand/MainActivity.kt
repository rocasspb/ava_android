package com.rocasspb.avaawaand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.mapbox.common.MapboxOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.RasterLayer
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.ImageSource
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.sources.updateImage
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.toCameraOptions
import com.rocasspb.avaawaand.logic.*
import com.rocasspb.avaawaand.utils.AvalancheConfig.MAX_DISTANCE_PITCHED
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var mapboxMap: MapboxMap? = null
    private var overlayJob: Job? = null

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

        setContent {
            AvaAwaAndTheme {
                MainScreen(viewModel)
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: MainViewModel) {
        val visualizationMode by viewModel.visualizationMode.observeAsState(VisualizationMode.BULLETIN)
        val pointInfo by viewModel.pointInfo.observeAsState()
        var showModePanel by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            MapContent(viewModel)

            if (pointInfo != null) {
                PointInfoCard(
                    pointInfo = pointInfo!!,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (!showModePanel) {
                    FloatingActionButton(
                        onClick = { viewModel.toggleMapStyle() },
                        containerColor = Color.White,
                        contentColor = Color(0xFF5F6368),
                        modifier = Modifier.padding(bottom = 12.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_layers),
                            contentDescription = "Switch Map Style",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { showModePanel = true },
                        containerColor = Color.White,
                        contentColor = Color(0xFF5F6368),
                        shape = CircleShape
                    ) {
                        val iconRes = when (visualizationMode) {
                            VisualizationMode.BULLETIN -> R.drawable.ic_bulletin
                            VisualizationMode.RISK -> R.drawable.ic_landscape
                            VisualizationMode.CUSTOM -> R.drawable.ic_custom
                        }
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = "Select Mode",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            if (showModePanel) {
                ModeSelectionPanel(
                    viewModel = viewModel,
                    onClose = { showModePanel = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }

    @Composable
    fun MapContent(viewModel: MainViewModel) {
        val mapStyleUrl by viewModel.mapStyleUrl.observeAsState(Style.OUTDOORS)
        val initialCameraPosition by viewModel.initialCameraPosition.observeAsState()
        val generationRules by viewModel.generationRules.observeAsState(emptyList())

        var lastLoadedStyle by remember { mutableStateOf<String?>(null) }
        var hasSetInitialCamera by remember { mutableStateOf(false) }

        // Trigger overlay update when rules change
        LaunchedEffect(generationRules) {
            mapboxMap?.let { map ->
                map.getStyle { style ->
                    overlayRaster(map, generationRules, style)
                }
            }
        }

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    val map = this.mapboxMap
                    this@MainActivity.mapboxMap = map
                    
                    map.subscribeMapIdle {
                        val rules = viewModel.generationRules.value ?: return@subscribeMapIdle
                        map.getStyle { style ->
                            overlayRaster(map, rules, style)
                        }
                    }

                    map.addOnMapLongClickListener { point ->
                        viewModel.getPointInfo(point, map.cameraState.zoom)
                        true
                    }

                    map.addOnMapClickListener {
                        viewModel.clearPointInfo()
                        false
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                val map = mapView.mapboxMap
                
                initialCameraPosition?.let {
                    if (!hasSetInitialCamera) {
                        map.setCamera(it)
                        hasSetInitialCamera = true
                    }
                }

                if (lastLoadedStyle != mapStyleUrl) {
                    lastLoadedStyle = mapStyleUrl
                    map.loadStyle(
                        styleExtension = style(mapStyleUrl) {
                            val demSourceId = "dem-source"
                            +rasterDemSource(demSourceId) {
                                url("mapbox://mapbox.mapbox-terrain-dem-v1")
                                tileSize(514)
                            }
                            +terrain(demSourceId)
                            +projection(ProjectionName.GLOBE)
                        }
                    ) { style ->
                        mapView.compass.enabled = false
                        mapView.scalebar.enabled = false
                        mapView.logo.enabled = true
                        mapView.attribution.enabled = true
                        mapView.gestures.pitchEnabled = true
                        
                        overlayRaster(map, generationRules, style)
                    }
                }
            }
        )
    }

    private fun overlayRaster(map: MapboxMap, rules: List<GenerationRule>, style: Style) {
        if (rules.isEmpty()) return

        val cameraState = map.cameraState
        val bounds = map.coordinateBoundsForCamera(cameraState.toCameraOptions())
        val center = cameraState.center
        val maxDelta = if (cameraState.pitch > 30) MAX_DISTANCE_PITCHED else 10.0

        val renderBounds = GeometryUtils.Bounds(
            max(bounds.west(), center.longitude() - maxDelta),
            min(bounds.east(), center.longitude() + maxDelta),
            max(bounds.south(), center.latitude() - maxDelta),
            min(bounds.north(), center.latitude() + maxDelta)
        )
        val zoom = cameraState.zoom

        overlayJob?.cancel()
        overlayJob = lifecycleScope.launch(Dispatchers.Default) {
            val provider = TerrainRgbElevationProvider()
            provider.prepare(renderBounds, zoom)

            val bitmap = RasterGenerator.drawToBitmap(rules, renderBounds, provider) ?: return@launch

            withContext(Dispatchers.Main) {
                if (style.isStyleLoaded()) {
                    val sourceId = "avalanche-source"
                    val layerId = "avalanche-layer"

                    if (style.styleSourceExists(sourceId)) {
                        style.removeStyleLayer(layerId)
                        style.removeStyleSource(sourceId)
                    }

                    val coords = listOf(
                        listOf(renderBounds.minLng, renderBounds.maxLat),
                        listOf(renderBounds.maxLng, renderBounds.maxLat),
                        listOf(renderBounds.maxLng, renderBounds.minLat),
                        listOf(renderBounds.minLng, renderBounds.minLat)
                    )

                    val imageSource = ImageSource.Builder(sourceId)
                        .coordinates(coords)
                        .build()
                    style.addSource(imageSource)
                    imageSource.updateImage(bitmap)

                    val layer = RasterLayer(layerId, sourceId)
                    layer.rasterOpacity(0.7)
                    style.addLayer(layer)
                }
            }
        }
    }

    @Composable
    fun PointInfoCard(pointInfo: MainViewModel.PointInfo, modifier: Modifier = Modifier) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = String.format(
                        Locale.US,
                        "Elev: %dm\nSlope: %.1f°\nAspect: %s",
                        pointInfo.elevation, pointInfo.slope, pointInfo.aspect
                    ),
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val map = mapboxMap ?: return
        val camera = map.cameraState
        val target = camera.center
        val mode = viewModel.visualizationMode.value ?: VisualizationMode.BULLETIN
        
        val prefs = getPreferences(MODE_PRIVATE)
        prefs.edit {
            putFloat("lat", target.latitude().toFloat())
            putFloat("lon", target.longitude().toFloat())
            putFloat("zoom", camera.zoom.toFloat())
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
