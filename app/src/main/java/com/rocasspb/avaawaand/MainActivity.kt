package com.rocasspb.avaawaand

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

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
            }
        })

        // Observe ViewModel for initial position
        viewModel.initialCameraPosition.observe(this, Observer { position ->
            if (position != null) {
                map.cameraPosition = position
            }
        })
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