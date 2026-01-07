package com.rocasspb.avaawaand

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import androidx.core.content.edit

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fabMode: FloatingActionButton
    private lateinit var panelModeSelection: CardView
    private lateinit var btnClosePanel: ImageView
    private lateinit var optionBulletin: LinearLayout
    private lateinit var bgBulletin: FrameLayout
    private lateinit var iconBulletin: ImageView
    private lateinit var textBulletin: TextView
    private lateinit var optionRisk: LinearLayout
    private lateinit var bgRisk: FrameLayout
    private lateinit var iconRisk: ImageView
    private lateinit var textRisk: TextView
    private lateinit var optionCustom: LinearLayout
    private lateinit var bgCustom: FrameLayout
    private lateinit var iconCustom: ImageView
    private lateinit var textCustom: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Init MapLibre
        MapLibre.getInstance(this)

        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        
        // Restore state
        val prefs = getPreferences(MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 47.26f).toDouble()
        val lon = prefs.getFloat("lon", 11.77f).toDouble()
        val zoom = prefs.getFloat("zoom", 8.0f).toDouble()
        val modeName = prefs.getString("mode", VisualizationMode.BULLETIN.name)
        val mode = try {
            VisualizationMode.valueOf(modeName ?: VisualizationMode.BULLETIN.name)
        } catch (e: Exception) {
            VisualizationMode.BULLETIN
        }
        
        viewModel.restoreState(lat, lon, zoom, mode)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        fabMode = findViewById(R.id.fabMode)
        panelModeSelection = findViewById(R.id.panelModeSelection)
        btnClosePanel = findViewById(R.id.btnClosePanel)

        optionBulletin = findViewById(R.id.optionBulletin)
        bgBulletin = findViewById(R.id.bgBulletin)
        iconBulletin = findViewById(R.id.iconBulletin)
        textBulletin = findViewById(R.id.textBulletin)

        optionRisk = findViewById(R.id.optionRisk)
        bgRisk = findViewById(R.id.bgRisk)
        iconRisk = findViewById(R.id.iconRisk)
        textRisk = findViewById(R.id.textRisk)

        optionCustom = findViewById(R.id.optionCustom)
        bgCustom = findViewById(R.id.bgCustom)
        iconCustom = findViewById(R.id.iconCustom)
        textCustom = findViewById(R.id.textCustom)

        // Observe visualization mode to update UI
        viewModel.visualizationMode.observe(this, Observer { mode ->
             updateModeSelectionUi(mode)
        })
    }

    private fun setupListeners() {
        fabMode.setOnClickListener {
            panelModeSelection.visibility = View.VISIBLE
            fabMode.hide()
        }

        btnClosePanel.setOnClickListener {
            panelModeSelection.visibility = View.GONE
            fabMode.show()
        }

        optionBulletin.setOnClickListener {
            viewModel.setVisualizationMode(VisualizationMode.BULLETIN)
        }

        optionRisk.setOnClickListener {
            viewModel.setVisualizationMode(VisualizationMode.RISK)
        }

        optionCustom.setOnClickListener {
            viewModel.setVisualizationMode(VisualizationMode.CUSTOM)
        }
    }

    private fun updateModeSelectionUi(mode: VisualizationMode) {
        // Reset all
        resetOptionUi(bgBulletin, iconBulletin, textBulletin)
        resetOptionUi(bgRisk, iconRisk, textRisk)
        resetOptionUi(bgCustom, iconCustom, textCustom)

        // Highlight selected
        when (mode) {
            VisualizationMode.BULLETIN -> {
                highlightOptionUi(bgBulletin, iconBulletin, textBulletin)
                fabMode.setImageResource(R.drawable.ic_bulletin)
            }
            VisualizationMode.RISK -> {
                highlightOptionUi(bgRisk, iconRisk, textRisk)
                fabMode.setImageResource(R.drawable.ic_landscape)
            }
            VisualizationMode.CUSTOM -> {
                highlightOptionUi(bgCustom, iconCustom, textCustom)
                fabMode.setImageResource(R.drawable.ic_custom)
            }
        }
    }

    private fun resetOptionUi(bg: FrameLayout, icon: ImageView, text: TextView) {
        bg.background = ContextCompat.getDrawable(this, android.R.color.transparent)
        icon.setColorFilter(ContextCompat.getColor(this, R.color.gray_unselected))
        text.setTextColor(ContextCompat.getColor(this, R.color.gray_unselected))
    }

    private fun highlightOptionUi(bg: FrameLayout, icon: ImageView, text: TextView) {
        bg.background = ContextCompat.getDrawable(this, R.drawable.bg_option_selected)
        icon.setColorFilter(ContextCompat.getColor(this, R.color.blue_selected))
        text.setTextColor(ContextCompat.getColor(this, R.color.blue_selected))
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
        
        val map = mapLibreMap ?: return
        val camera = map.cameraPosition
        val target = camera.target ?: return
        val mode = viewModel.visualizationMode.value ?: VisualizationMode.BULLETIN
        
        val prefs = getPreferences(MODE_PRIVATE)
        prefs.edit {
            putFloat("lat", target.latitude.toFloat())
            putFloat("lon", target.longitude.toFloat())
            putFloat("zoom", camera.zoom.toFloat())
            putString("mode", mode.name)
        }
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
