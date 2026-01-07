package com.rocasspb.avaawaand

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.rocasspb.avaawaand.logic.GenerationRule
import com.rocasspb.avaawaand.logic.RasterGenerator
import com.rocasspb.avaawaand.logic.TerrainRgbElevationProvider
import com.rocasspb.avaawaand.logic.VisualizationMode
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngQuad
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.ImageSource
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Init MapLibre
        MapLibre.getInstance(this)
        
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        
        // Observe ViewModel for style URL
        viewModel.mapStyleUrl.observe(this, Observer { styleUrl ->
            map.setStyle(styleUrl) { style ->
                setupMapUiSettings(map)
                
                // Re-apply raster if rules exist and style changed
                val rules = viewModel.generationRules.value
                if (rules != null) {
                    overlayRaster(rules, style)
                }
            }
        })

        // Observe ViewModel for initial position
        viewModel.initialCameraPosition.observe(this, Observer { position ->
            if (position != null) {
                map.cameraPosition = position
            }
        })
        
        // Observe generation rules
        viewModel.generationRules.observe(this, Observer { rules ->
            map.getStyle { style ->
                overlayRaster(rules, style)
            }
        })

        // Recalculate raster on camera idle (zoom/pan)
        map.addOnCameraIdleListener {
            val rules = viewModel.generationRules.value ?: return@addOnCameraIdleListener
            map.getStyle { style ->
                overlayRaster(rules, style)
            }
        }

        // Set mode to BULLETIN
        viewModel.setVisualizationMode(VisualizationMode.BULLETIN)
    }

    private fun setupMapUiSettings(map: MapLibreMap) {
        map.uiSettings.isCompassEnabled = true
        map.uiSettings.isLogoEnabled = true
        map.uiSettings.isAttributionEnabled = true
        map.uiSettings.isRotateGesturesEnabled = true
        map.uiSettings.isTiltGesturesEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true
        map.uiSettings.isScrollGesturesEnabled = true
    }
    
    private fun overlayRaster(rules: List<GenerationRule>, style: Style) {
        if (rules.isEmpty()) return

        val map = mapLibreMap ?: return
        val visibleBounds = map.projection.visibleRegion.latLngBounds
        val renderBounds = GeometryUtils.Bounds(
            visibleBounds.longitudeWest,
            visibleBounds.longitudeEast,
            visibleBounds.latitudeSouth,
            visibleBounds.latitudeNorth
        )
        val zoom = map.cameraPosition.zoom

        lifecycleScope.launch(Dispatchers.Default) {
            val provider = TerrainRgbElevationProvider()
            provider.prepare(renderBounds, zoom)
            
            // Generate bitmap for the current visible bounds
            val bitmap = RasterGenerator.drawToBitmap(rules, renderBounds, provider) ?: return@launch
            
            withContext(Dispatchers.Main) {
                if (style.isFullyLoaded) {
                    val sourceId = "avalanche-source"
                    val layerId = "avalanche-layer"
                    
                    // Cleanup existing
                    if (style.getSource(sourceId) != null) {
                        style.removeLayer(layerId)
                        style.removeSource(sourceId)
                    }
                    
                    val quad = LatLngQuad(
                        LatLng(renderBounds.maxLat, renderBounds.minLng),
                        LatLng(renderBounds.maxLat, renderBounds.maxLng),
                        LatLng(renderBounds.minLat, renderBounds.maxLng),
                        LatLng(renderBounds.minLat, renderBounds.minLng)
                    )
                    
                    val source = ImageSource(sourceId, quad, bitmap)
                    style.addSource(source)
                    
                    val layer = RasterLayer(layerId, sourceId)
                    layer.setProperties(
                        PropertyFactory.rasterOpacity(0.7f)
                    )
                    style.addLayer(layer)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}
