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
import androidx.core.content.edit
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
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
import com.rocasspb.avaawaand.logic.CustomModeParams
import com.rocasspb.avaawaand.logic.GenerationRule
import com.rocasspb.avaawaand.logic.RasterGenerator
import com.rocasspb.avaawaand.logic.TerrainRgbElevationProvider
import com.rocasspb.avaawaand.logic.VisualizationMode
import com.rocasspb.avaawaand.utils.AvalancheConfig.MAX_DISTANCE_PITCHED
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var mapboxMap: MapboxMap? = null
    private val viewModel: MainViewModel by viewModels()
    private lateinit var fabMode: FloatingActionButton
    private lateinit var fabLayers: FloatingActionButton
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
    
    private lateinit var customControls: LinearLayout
    private lateinit var sliderElevation: RangeSlider
    private lateinit var sliderSteepness: Slider
    private lateinit var chipGroupAspects: ChipGroup

    private lateinit var cardPointInfo: CardView
    private lateinit var textPointInfo: TextView

    private var overlayJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapboxOptions.accessToken=BuildConfig.MAPBOX_ACCESS_TOKEN
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapboxMap = mapView?.mapboxMap
        
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
        
        // Observe ViewModel for style URL
        viewModel.mapStyleUrl.observe(this, Observer { styleUrl ->
            mapboxMap?.loadStyle(
                styleExtension = style(styleUrl) {
                    val dem_source_id = "dem-source"
                    +rasterDemSource(dem_source_id) {
                        url("mapbox://mapbox.mapbox-terrain-dem-v1")
                        // 514 specifies padded DEM tile and provides better performance than 512 tiles.
                        tileSize(514)
                    }
                    +terrain(dem_source_id)
                    +projection(ProjectionName.GLOBE)
                }
            ) { style ->
                setupMapUiSettings()
                val rules = viewModel.generationRules.value
                if (rules != null) {
                    overlayRaster(rules, style)
                }
            }
        })
        
        // Observe ViewModel for initial position
        viewModel.initialCameraPosition.observe(this, Observer { options ->
             if (options != null) {
                 mapboxMap?.setCamera(options)
             }
        })
        
        // Observe generation rules
        viewModel.generationRules.observe(this, Observer { rules ->
            mapboxMap?.getStyle { style ->
                 overlayRaster(rules, style)
            }
        })
        
        // Observe point info
        viewModel.pointInfo.observe(this, Observer { info ->
            if (info != null) {
                textPointInfo.text = String.format(
                    Locale.US,
                    "Elev: %dm\nSlope: %.1f°\nAspect: %s",
                    info.elevation, info.slope, info.aspect
                )
                cardPointInfo.visibility = View.VISIBLE
            } else {
                cardPointInfo.visibility = View.GONE
            }
        })
        
        // Using onMapIdle listener for performance
        mapboxMap?.addOnMapIdleListener {
             val rules = viewModel.generationRules.value ?: return@addOnMapIdleListener
             mapboxMap?.getStyle { style ->
                 overlayRaster(rules, style)
             }
        }
    }

    private fun setupViews() {
        fabMode = findViewById(R.id.fabMode)
        fabLayers = findViewById(R.id.fabLayers)
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
        
        customControls = findViewById(R.id.customControls)
        sliderElevation = findViewById(R.id.sliderElevation)
        sliderSteepness = findViewById(R.id.sliderSteepness)
        chipGroupAspects = findViewById(R.id.chipGroupAspects)

        cardPointInfo = findViewById(R.id.cardPointInfo)
        textPointInfo = findViewById(R.id.textPointInfo)

        // Set label formatters to show only integer values
        sliderElevation.setLabelFormatter { value -> value.toInt().toString() }
        sliderSteepness.setLabelFormatter { value -> value.toInt().toString() }

        // Observe visualization mode to update UI
        viewModel.visualizationMode.observe(this, Observer { mode ->
             updateModeSelectionUi(mode)
        })
        
        // Initializing chips
        val aspects = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        aspects.forEach { aspect ->
            for (i in 0 until chipGroupAspects.childCount) {
                val chip = chipGroupAspects.getChildAt(i) as? Chip
                if (chip?.text == aspect) {
                    chip.isChecked = true
                }
            }
        }
    }

    private fun setupListeners() {
        fabMode.setOnClickListener {
            panelModeSelection.visibility = View.VISIBLE
            fabMode.hide()
            fabLayers.hide()
        }

        fabLayers.setOnClickListener {
            viewModel.toggleMapStyle()
        }

        btnClosePanel.setOnClickListener {
            panelModeSelection.visibility = View.GONE
            fabMode.show()
            fabLayers.show()
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
        
        sliderElevation.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                updateViewModelCustomParams()
            }
        }
        
        sliderSteepness.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                updateViewModelCustomParams()
            }
        }
        
        for (i in 0 until chipGroupAspects.childCount) {
            val chip = chipGroupAspects.getChildAt(i) as? Chip
            chip?.setOnCheckedChangeListener { _, _ ->
                updateViewModelCustomParams()
            }
        }

        mapboxMap?.addOnMapLongClickListener { point ->
            viewModel.getPointInfo(point, mapboxMap?.cameraState?.zoom ?: 12.0)
            true
        }

        mapboxMap?.addOnMapClickListener {
            viewModel.clearPointInfo()
            false
        }
    }
    
    private fun updateViewModelCustomParams() {
        val elevationValues = sliderElevation.values
        val minElev = elevationValues[0].toInt()
        val maxElev = elevationValues[1].toInt()
        val minSlope = sliderSteepness.value.toInt()
        
        val selectedAspects = mutableListOf<String>()
        for (i in 0 until chipGroupAspects.childCount) {
            val chip = chipGroupAspects.getChildAt(i) as? Chip
            if (chip != null && chip.isChecked) {
                selectedAspects.add(chip.text.toString())
            }
        }
        
        viewModel.updateCustomParams(CustomModeParams(
            minElev = minElev,
            maxElev = maxElev,
            minSlope = minSlope,
            aspects = selectedAspects
        ))
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
                customControls.visibility = View.GONE
            }
            VisualizationMode.RISK -> {
                highlightOptionUi(bgRisk, iconRisk, textRisk)
                fabMode.setImageResource(R.drawable.ic_landscape)
                customControls.visibility = View.GONE
            }
            VisualizationMode.CUSTOM -> {
                highlightOptionUi(bgCustom, iconCustom, textCustom)
                fabMode.setImageResource(R.drawable.ic_custom)
                customControls.visibility = View.VISIBLE
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

    private fun setupMapUiSettings() {
        mapView?.compass?.enabled = true
        mapView?.scalebar?.enabled = true
        mapView?.logo?.enabled = true
        mapView?.attribution?.enabled = true
        mapView?.gestures?.pitchEnabled = true
    }

    private fun overlayRaster(rules: List<GenerationRule>, style: Style) {
        if (rules.isEmpty()) return

        val map = mapboxMap ?: return
        val cameraState = map.cameraState
        val bounds = map.coordinateBoundsForCamera(cameraState.toCameraOptions())
        
        val center = cameraState.center
        //in high-pitch scenarios, bounds appear enormous - therefore we need to cap to a
        //reasonable distance for calculation. At low pitch it is not possible to have this problem.
        //The parameters are arbitrary though
        val maxDelta = if(cameraState.pitch > 30) MAX_DISTANCE_PITCHED else 10.0
        
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

            // Generate bitmap for the current visible bounds
            val bitmap = RasterGenerator.drawToBitmap(rules, renderBounds, provider) ?: return@launch

            withContext(Dispatchers.Main) {
                if (style.isStyleLoaded()) {
                    val sourceId = "avalanche-source"
                    val layerId = "avalanche-layer"

                    // Cleanup existing
                    if (style.styleSourceExists(sourceId)) {
                        style.removeStyleLayer(layerId)
                        style.removeStyleSource(sourceId)
                    }

                    val coords = listOf(
                        listOf(renderBounds.minLng, renderBounds.maxLat), // Top Left
                        listOf(renderBounds.maxLng, renderBounds.maxLat), // Top Right
                        listOf(renderBounds.maxLng, renderBounds.minLat), // Bottom Right
                        listOf(renderBounds.minLng, renderBounds.minLat)  // Bottom Left
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
