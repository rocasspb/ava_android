package com.rocasspb.avaawaand

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rocasspb.avaawaand.data.AvalancheData
import com.rocasspb.avaawaand.data.MainRepository
import com.rocasspb.avaawaand.data.MainRepositoryImpl
import com.rocasspb.avaawaand.data.RegionResponse
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng

class MainViewModel(private val repository: MainRepository = MainRepositoryImpl()) : ViewModel() {

    private val _mapStyleUrl = MutableLiveData<String>()
    val mapStyleUrl: LiveData<String> = _mapStyleUrl

    private val _initialCameraPosition = MutableLiveData<CameraPosition>()
    val initialCameraPosition: LiveData<CameraPosition> = _initialCameraPosition

    private val _regions = MutableLiveData<RegionResponse>()
    val regions: LiveData<RegionResponse> = _regions

    private val _avalancheData = MutableLiveData<List<AvalancheData>>()
    val avalancheData: LiveData<List<AvalancheData>> = _avalancheData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // Load initial data
        loadMapConfig()
        fetchData()
    }

    fun loadMapConfig() {
        val apiKey = BuildConfig.MAPTILER_KEY
        _mapStyleUrl.value = "https://api.maptiler.com/maps/winter-v2/style.json?key=${apiKey}"
        
        _initialCameraPosition.value = CameraPosition.Builder()
            .target(LatLng(47.26, 11.77))
            .zoom(8.0)
            .build()
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                _error.value = null
                val regionsResponse = repository.getRegions()
                _regions.value = regionsResponse

                val avalancheResponse = repository.getAvalancheData()
                _avalancheData.value = avalancheResponse.bulletins
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
