package com.rocasspb.avaawaand

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap as MapboxMapCompose
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
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
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.toCameraOptions
import com.rocasspb.avaawaand.logic.RasterGenerator
import com.rocasspb.avaawaand.logic.TerrainRgbElevationProvider
import com.rocasspb.avaawaand.logic.VisualizationMode
import com.rocasspb.avaawaand.utils.AvalancheConfig.MAX_DISTANCE_PITCHED
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    requestPermissions: Boolean = true,
    mapContent: @Composable (MapViewportState, (Boolean) -> Unit) -> Unit = { mapViewportState, onLoadingChange ->
        MapContent(
            viewModel = viewModel,
            mapViewportState = mapViewportState,
            onLoadingChange = onLoadingChange
        )
    }
) {
    val context = LocalContext.current
    if (requestPermissions) {
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            viewModel.setLocationPermissionGranted(granted)
        }

        LaunchedEffect(Unit) {
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFineLocation || hasCoarseLocation) {
                viewModel.setLocationPermissionGranted(true)
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    val visualizationMode by viewModel.visualizationMode.collectAsStateWithLifecycle()
    val pointInfo by viewModel.pointInfo.collectAsStateWithLifecycle()
    val selectedGpxTrack by viewModel.selectedGpxTrack.collectAsStateWithLifecycle()
    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsStateWithLifecycle()
    val initialCameraPosition by viewModel.initialCameraPosition.collectAsStateWithLifecycle()
    val showDisclaimer by viewModel.showDisclaimer.collectAsStateWithLifecycle()
    var showModePanel by remember { mutableStateOf(false) }
    var isOverlayLoading by remember { mutableStateOf(false) }

    val mapViewportState = rememberMapViewportState {
        initialCameraPosition?.let {
            setCameraOptions(it)
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val gpxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    viewModel.importGpx(bytes.inputStream())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        mapContent(mapViewportState) { isOverlayLoading = it }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOverlayLoading) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(32.dp).testTag("OverlayLoadingIndicator")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (pointInfo != null) {
                PointInfoCard(pointInfo = pointInfo!!)
            }

            if (selectedGpxTrack != null) {
                GpxInfoCard(
                    track = selectedGpxTrack!!,
                    onDeleteClick = { viewModel.deleteGpx(selectedGpxTrack!!.id) }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (!showModePanel) {
                if (locationPermissionGranted) {
                    FloatingActionButton(
                        onClick = {
                            val currentBearing = mapViewportState.cameraState?.bearing ?: 0.0
                            val currentPitch = mapViewportState.cameraState?.pitch ?: 0.0
                            mapViewportState.transitionToFollowPuckState(
                                FollowPuckViewportStateOptions.Builder()
                                    .bearing(FollowPuckViewportStateBearing.Constant(currentBearing))
                                    .pitch(currentPitch)
                                    .zoom(12.0)
                                    .build()
                            )
                        },
                        containerColor = Color.White,
                        contentColor = Color(0xFF5F6368),
                        modifier = Modifier.padding(bottom = 12.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                val currentPitch = mapViewportState.cameraState?.pitch ?: 0.0
                val is3D = currentPitch > 0.0

                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            mapViewportState.easeTo(
                                CameraOptions.Builder()
                                    .pitch(if (is3D) 0.0 else 60.0)
                                    .build()
                            )
                        }
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF5F6368),
                    modifier = Modifier.padding(bottom = 12.dp),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (is3D) "2D" else "3D",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

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
                    onClick = { gpxLauncher.launch("*/*") },
                    containerColor = Color.White,
                    contentColor = Color(0xFF5F6368),
                    modifier = Modifier.padding(bottom = 12.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gpx),
                        contentDescription = "Import GPX",
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
                        VisualizationMode.OFF -> R.drawable.ic_off
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

        if (showDisclaimer) {
            DisclaimerDialog(
                onConfirm = { viewModel.acceptDisclaimer() }
            )
        }
    }
}

@Composable
fun MapContent(
    viewModel: MainViewModel,
    mapViewportState: MapViewportState,
    onLoadingChange: (Boolean) -> Unit
) {
    val mapStyleUrl by viewModel.mapStyleUrl.collectAsStateWithLifecycle()
    val generationRules by viewModel.generationRules.collectAsStateWithLifecycle()
    val gpxTracks by viewModel.gpxTracks.collectAsStateWithLifecycle()
    val selectedGpxTrack by viewModel.selectedGpxTrack.collectAsStateWithLifecycle()
    val locationPermissionGranted by viewModel.locationPermissionGranted.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var overlayJob by remember { mutableStateOf<Job?>(null) }

    // Sync viewport state back to ViewModel for persistence
    LaunchedEffect(mapViewportState.cameraState) {
        mapViewportState.cameraState?.let {
            viewModel.updateCameraPosition(it.toCameraOptions())
        }
    }

    var overlayBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var overlayCoords by remember { mutableStateOf<List<List<Double>>?>(null) }
    var mapboxMapInstance by remember { mutableStateOf<MapboxMap?>(null) }
    var styleLoadedCount by remember { mutableStateOf(0) }

    // Trigger overlay update when rules, camera, or map instance change
    LaunchedEffect(generationRules, mapViewportState.cameraState, mapboxMapInstance) {
        val map = mapboxMapInstance ?: return@LaunchedEffect
        val cameraState = mapViewportState.cameraState ?: return@LaunchedEffect
        if (generationRules.isEmpty()) {
            overlayBitmap = null
            return@LaunchedEffect
        }

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
        onLoadingChange(true)
        overlayJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                val provider = TerrainRgbElevationProvider()
                provider.prepare(renderBounds, zoom)

                val bitmap = RasterGenerator.drawToBitmap(generationRules, renderBounds, provider)
                if (bitmap != null) {
                    val coords = listOf(
                        listOf(renderBounds.minLng, renderBounds.maxLat),
                        listOf(renderBounds.maxLng, renderBounds.maxLat),
                        listOf(renderBounds.maxLng, renderBounds.minLat),
                        listOf(renderBounds.minLng, renderBounds.minLat)
                    )
                    withContext(Dispatchers.Main) {
                        overlayBitmap = bitmap
                        overlayCoords = coords
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    onLoadingChange(false)
                }
            }
        }
    }

    MapboxMapCompose(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        onMapClickListener = OnMapClickListener {
            viewModel.clearPointInfo()
            viewModel.deselectGpxTrack()
            false
        },
        onMapLongClickListener = OnMapLongClickListener { point ->
            mapViewportState.cameraState?.let {
                viewModel.getPointInfo(point, it.zoom)
            }
            true
        },
        scaleBar = { ScaleBar(modifier = Modifier.padding(vertical = 30.dp)) },
        compass = { Compass(modifier = Modifier.padding(vertical = 30.dp)) }
    ) {
        GpxOverlay(
            gpxTracks = gpxTracks,
            selectedTrack = selectedGpxTrack,
            onTrackClick = { viewModel.selectGpxTrack(it) }
        )

        MapEffect(locationPermissionGranted) { mapView ->
            mapView.location.apply {
                enabled = locationPermissionGranted
                locationPuck = createDefault2DPuck(withBearing = true)
                puckBearingEnabled = true
                puckBearing = PuckBearing.HEADING
            }
        }

        MapEffect(mapStyleUrl) { mapView ->
            val map = mapView.mapboxMap
            mapboxMapInstance = map

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
            ) {
                mapView.gestures.pitchEnabled = true
                styleLoadedCount++
            }
        }

        MapEffect(overlayBitmap, overlayCoords, styleLoadedCount) { mapView ->
            val style = mapView.mapboxMap.style ?: return@MapEffect
            val bitmap = overlayBitmap
            val coords = overlayCoords

            val sourceId = "avalanche-source"
            val layerId = "avalanche-layer"

            if (bitmap == null || coords == null) {
                if (style.styleSourceExists(sourceId)) {
                    style.removeStyleLayer(layerId)
                    style.removeStyleSource(sourceId)
                }
                return@MapEffect
            }

            if (style.styleSourceExists(sourceId)) {
                style.removeStyleLayer(layerId)
                style.removeStyleSource(sourceId)
            }

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
