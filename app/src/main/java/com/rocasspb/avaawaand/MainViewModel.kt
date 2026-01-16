package com.rocasspb.avaawaand

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.rocasspb.avaawaand.data.AvalancheData
import com.rocasspb.avaawaand.data.MainRepository
import com.rocasspb.avaawaand.data.MainRepositoryImpl
import com.rocasspb.avaawaand.data.RegionResponse
import com.rocasspb.avaawaand.logic.AvalancheLogic
import com.rocasspb.avaawaand.logic.CustomModeParams
import com.rocasspb.avaawaand.logic.GenerationRule
import com.rocasspb.avaawaand.logic.RuleProperties
import com.rocasspb.avaawaand.logic.VisualizationMode
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.toString

class MainViewModel(private val repository: MainRepository = MainRepositoryImpl()) : ViewModel() {

    private val _mapStyleUrl = MutableLiveData<String>()
    val mapStyleUrl: LiveData<String> = _mapStyleUrl

    private val _initialCameraPosition = MutableLiveData<CameraOptions>()
    val initialCameraPosition: LiveData<CameraOptions> = _initialCameraPosition

    private val _regions = MutableLiveData<RegionResponse>()
    val regions: LiveData<RegionResponse> = _regions

    private val _avalancheData = MutableLiveData<List<AvalancheData>>()
    val avalancheData: LiveData<List<AvalancheData>> = _avalancheData

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _generationRules = MutableLiveData<List<GenerationRule>>()
    val generationRules: LiveData<List<GenerationRule>> = _generationRules

    private val _visualizationMode = MutableLiveData<VisualizationMode>(VisualizationMode.BULLETIN)
    val visualizationMode: LiveData<VisualizationMode> = _visualizationMode

    private val _customModeParams = MutableLiveData<CustomModeParams>(CustomModeParams())
    val customModeParams: LiveData<CustomModeParams> = _customModeParams

    private var calculationJob: Job? = null

    init {
        // Load initial data
        loadMapConfig()
        fetchData()
    }

    fun loadMapConfig() {
        _mapStyleUrl.value = Style.SATELLITE
        
        _initialCameraPosition.value = CameraOptions.Builder()
            .center(Point.fromLngLat(11.77, 47.26))
            .zoom(8.0)
            .build()
    }

    fun restoreState(lat: Double, lon: Double, zoom: Double, mode: VisualizationMode) {
        _initialCameraPosition.value = CameraOptions.Builder()
            .center(Point.fromLngLat(lon, lat))
            .zoom(zoom)
            .build()
        
        if (_visualizationMode.value != mode) {
            _visualizationMode.value = mode
            calculateRules()
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                _error.value = null
                val regionsResponse = repository.getRegions()
                _regions.value = regionsResponse

                val avalancheResponse = repository.getAvalancheData()
                _avalancheData.value = avalancheResponse.bulletins
                
                calculateRules()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun setVisualizationMode(mode: VisualizationMode) {
        if (_visualizationMode.value != mode) {
            _visualizationMode.value = mode
            calculateRules()
        }
    }

    fun updateCustomParams(params: CustomModeParams) {
        _customModeParams.value = params
        if (_visualizationMode.value == VisualizationMode.CUSTOM) {
            calculateRules()
        }
    }
    
    fun calculateRules() {
         calculationJob?.cancel()
         calculationJob = viewModelScope.launch(Dispatchers.Default) {
             val bulletins = _avalancheData.value ?: return@launch
             val regions = _regions.value ?: return@launch
             val currentMode = _visualizationMode.value ?: VisualizationMode.BULLETIN

             if(currentMode == VisualizationMode.CUSTOM) {
                 val customParams = _customModeParams.value ?: CustomModeParams()
                 val rules = AvalancheConfig.STEEPNESS_THRESHOLDS.map {
                     GenerationRule(
                         bounds = AvalancheConfig.EUREGIO_BOUNDS,
                         geometry = null,
                         minElev = customParams.minElev,
                         maxElev = customParams.maxElev,
                         minSlope = max(it.minSlope, customParams.minSlope),
                         validAspects = customParams.aspects,
                         color = it.color,
                         properties = RuleProperties(
                             steepness = it.label
                         )
                     )
                 }

                 _generationRules.postValue(rules)
             } else {
                 val bands = AvalancheLogic.processRegionElevations(bulletins)
                 val rules = AvalancheLogic.generateRules(
                     bands,
                     regions.features,
                     currentMode
                 )
                 _generationRules.postValue(rules)
             }
         }
    }
}
