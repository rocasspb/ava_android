package com.rocasspb.avaawaand

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.rocasspb.avaawaand.data.AvalancheActivity
import com.rocasspb.avaawaand.data.AvalancheData
import com.rocasspb.avaawaand.data.GpxRepository
import com.rocasspb.avaawaand.data.GpxRepositoryImpl
import com.rocasspb.avaawaand.data.GpxTrack
import com.rocasspb.avaawaand.data.MainRepository
import com.rocasspb.avaawaand.data.MainRepositoryImpl
import com.rocasspb.avaawaand.data.PersistenceManager
import com.rocasspb.avaawaand.data.RegionResponse
import com.rocasspb.avaawaand.logic.AvalancheLogic
import com.rocasspb.avaawaand.logic.CustomModeParams
import com.rocasspb.avaawaand.logic.GenerationRule
import com.rocasspb.avaawaand.logic.RuleProperties
import com.rocasspb.avaawaand.logic.TerrainRgbElevationProvider
import com.rocasspb.avaawaand.logic.TerrainUtils
import com.rocasspb.avaawaand.logic.VisualizationMode
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max

class MainViewModel(
    private val repository: MainRepository,
    private val gpxRepository: GpxRepository,
    private val elevationProvider: TerrainRgbElevationProvider = TerrainRgbElevationProvider(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _mapStyleUrl = MutableLiveData<String>()
    val mapStyleUrl: LiveData<String> = _mapStyleUrl

    private val _initialCameraPosition = MutableLiveData<CameraOptions>()
    val initialCameraPosition: LiveData<CameraOptions> = _initialCameraPosition

    private val _cameraPosition = MutableLiveData<CameraOptions>()
    val cameraPosition: LiveData<CameraOptions> = _cameraPosition

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

    private val _pointInfo = MutableLiveData<PointInfo?>()
    val pointInfo: LiveData<PointInfo?> = _pointInfo

    private val _locationPermissionGranted = MutableLiveData<Boolean>(false)
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted

    private val _gpxTracks = MutableLiveData<List<GpxTrack>>(emptyList())
    val gpxTracks: LiveData<List<GpxTrack>> = _gpxTracks

    private val _selectedGpxTrack = MutableLiveData<GpxTrack?>(null)
    val selectedGpxTrack: LiveData<GpxTrack?> = _selectedGpxTrack

    data class PointInfo(
        val elevation: Int,
        val slope: Double,
        val aspect: String,
        val dangerRatings: List<com.rocasspb.avaawaand.data.DangerRating>? = null,
        val avalancheProblems: List<com.rocasspb.avaawaand.data.AvalancheProblem>? = null,
        val avalancheActivity: AvalancheActivity? = null
    )

    private var calculationJob: Job? = null
    private var pointInfoJob: Job? = null

    init {
        // Load initial data
        loadMapConfig()
        fetchData()
        loadGpxTracks()
    }

    fun loadMapConfig() {
        _mapStyleUrl.value = Style.OUTDOORS
        
        _initialCameraPosition.value = CameraOptions.Builder()
            .center(Point.fromLngLat(11.77, 47.26))
            .zoom(8.0)
            .build()
    }

    fun toggleMapStyle() {
        _mapStyleUrl.value = if (_mapStyleUrl.value == Style.OUTDOORS) {
            Style.SATELLITE
        } else {
            Style.OUTDOORS
        }
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
            val persistedRegions = repository.getPersistedRegions()
            val persistedAvalanche = repository.getPersistedAvalancheData()
            
            if (persistedRegions != null && persistedAvalanche != null) {
                val hasFreshData = persistedAvalanche.bulletins.any { repository.isFresh(it) }
                if (hasFreshData) {
                    _regions.value = persistedRegions
                    _avalancheData.value = persistedAvalanche.bulletins
                    Log.d("MainViewModel", "Have fresh data, ${persistedAvalanche.bulletins.size} bulletins")
                    calculateRules()
                }
            }

            try {
                Log.d("MainViewModel", "Loading data")
                _error.value = null
                val regionsResponse = repository.getRegions()
                repository.persistRegions(regionsResponse)
                _regions.value = regionsResponse

                val avalancheResponse = repository.getAvalancheData()
                repository.persistAvalancheData(avalancheResponse)
                _avalancheData.value = avalancheResponse.bulletins
                Log.d("MainViewModel", "Data loaded")
                calculateRules()
            } catch (e: Exception) {
                if (_avalancheData.value == null) {
                    _error.value = e.message
                    Log.e("MainViewModel", "Failed to load data: ${_error.value}")
                }
            }
        }
    }

    fun loadGpxTracks() {
        viewModelScope.launch(ioDispatcher) {
            val tracks = gpxRepository.getAllTracks()
            _gpxTracks.postValue(tracks)
        }
    }

    fun selectGpxTrack(track: GpxTrack) {
        _selectedGpxTrack.value = track
    }

    fun deselectGpxTrack() {
        _selectedGpxTrack.value = null
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

    fun updateCameraPosition(cameraOptions: CameraOptions) {
        _cameraPosition.value = cameraOptions
        cameraOptions.zoom?.let { handleZoomChange(it) }
    }

    private fun handleZoomChange(zoom: Double) {
        val currentMode = _visualizationMode.value ?: return
        if (currentMode == VisualizationMode.OFF) return

        if (zoom >= 10.0) {
            if (currentMode == VisualizationMode.BULLETIN) {
                setVisualizationMode(VisualizationMode.RISK)
            }
        } else {
            if (currentMode == VisualizationMode.RISK || currentMode == VisualizationMode.CUSTOM) {
                setVisualizationMode(VisualizationMode.BULLETIN)
            }
        }
    }
    
    fun calculateRules() {
         // Capture current state values on the caller thread (usually Main) 
         // to avoid race conditions when reading LiveData.value inside the coroutine
         val bulletins = _avalancheData.value ?: return
         val regions = _regions.value ?: return
         val mode = _visualizationMode.value ?: VisualizationMode.BULLETIN
         val customParams = _customModeParams.value ?: CustomModeParams()

         calculationJob?.cancel()
         calculationJob = viewModelScope.launch(defaultDispatcher) {
             if (mode == VisualizationMode.OFF) {
                 _generationRules.postValue(emptyList())
                 return@launch
             }

             if(mode == VisualizationMode.CUSTOM) {
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
                     mode
                 )
                 _generationRules.postValue(rules)
             }
         }
    }

    fun getPointInfo(point: Point, zoom: Double) {
        pointInfoJob?.cancel()
        pointInfoJob = viewModelScope.launch(defaultDispatcher) {
            val geoPoint = GeometryUtils.Point(point.longitude(), point.latitude())
            
            // Prepare elevation provider for the small area around the point
            val bounds = GeometryUtils.Bounds(
                point.longitude() - 0.001,
                point.longitude() + 0.001,
                point.latitude() - 0.001,
                point.latitude() + 0.001
            )
            elevationProvider.prepare(bounds, zoom)

            val elevation = elevationProvider.getElevation(geoPoint) ?: return@launch
            val metrics = TerrainUtils.calculateTerrainMetrics(geoPoint) { p ->
                elevationProvider.getElevation(p)
            } ?: return@launch

            // Extract avalanche details
            val regions = _regions.value?.features ?: emptyList()
            val bulletins = _avalancheData.value ?: emptyList()
            
            val containingRegion = regions.find { 
                GeometryUtils.isPointInGeometry(geoPoint, it.geometry) 
            }
            
            var dangerRatings: List<com.rocasspb.avaawaand.data.DangerRating>? = null
            var problems: List<com.rocasspb.avaawaand.data.AvalancheProblem>? = null
            var avalancheActivity: AvalancheActivity? = null
            
            if (containingRegion != null) {
                val regionId = containingRegion.properties.id
                val relevantBulletin = bulletins.find { bulletin -> 
                    bulletin.regions.any { it.id.startsWith(regionId) }
                }
                
                if (relevantBulletin != null) {
                    avalancheActivity = relevantBulletin.avalancheActivity
                    dangerRatings = relevantBulletin.dangerRatings?.sortedByDescending {
                        getDangerNumericValue(it.mainValue) 
                    }
                    problems = relevantBulletin.avalancheProblems
                }
            }

            _pointInfo.postValue(PointInfo(elevation, metrics.slope, metrics.aspect, dangerRatings, problems, avalancheActivity))
        }
    }

    private fun getDangerNumericValue(dangerLevel: String): Int {
        return when (dangerLevel.lowercase()) {
            "low", "1" -> 1
            "moderate", "2" -> 2
            "considerable", "3" -> 3
            "high", "4" -> 4
            "very_high", "5" -> 5
            else -> 0
        }
    }

    fun clearPointInfo() {
        _pointInfo.postValue(null)
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val persistenceManager = PersistenceManager(application)
                val repository = MainRepositoryImpl(persistenceManager = persistenceManager)
                val gpxRepository = GpxRepositoryImpl(application)
                return MainViewModel(repository, gpxRepository) as T
            }
        }
    }
}
