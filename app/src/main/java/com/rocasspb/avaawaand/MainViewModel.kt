package com.rocasspb.avaawaand

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.rocasspb.avaawaand.data.AvalancheActivity
import com.rocasspb.avaawaand.data.AvalancheData
import com.rocasspb.avaawaand.data.GpxParser
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.math.max

class MainViewModel(
    private val repository: MainRepository,
    private val gpxRepository: GpxRepository,
    private val elevationProvider: TerrainRgbElevationProvider = TerrainRgbElevationProvider(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    private val gpxParser = GpxParser()

    private val _mapStyleUrl = MutableStateFlow(Style.OUTDOORS)
    val mapStyleUrl: StateFlow<String> = _mapStyleUrl.asStateFlow()

    private val _initialCameraPosition = MutableStateFlow<CameraOptions?>(null)
    val initialCameraPosition: StateFlow<CameraOptions?> = _initialCameraPosition.asStateFlow()

    private val _cameraPosition = MutableStateFlow<CameraOptions?>(null)
    val cameraPosition: StateFlow<CameraOptions?> = _cameraPosition.asStateFlow()

    private val _regions = MutableStateFlow<RegionResponse?>(null)
    val regions: StateFlow<RegionResponse?> = _regions.asStateFlow()

    private val _avalancheData = MutableStateFlow<List<AvalancheData>>(emptyList())
    val avalancheData: StateFlow<List<AvalancheData>> = _avalancheData.asStateFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error.asSharedFlow()

    private val _generationRules = MutableStateFlow<List<GenerationRule>>(emptyList())
    val generationRules: StateFlow<List<GenerationRule>> = _generationRules.asStateFlow()

    private val _visualizationMode = MutableStateFlow(VisualizationMode.BULLETIN)
    val visualizationMode: StateFlow<VisualizationMode> = _visualizationMode.asStateFlow()

    private val _customModeParams = MutableStateFlow(CustomModeParams())
    val customModeParams: StateFlow<CustomModeParams> = _customModeParams.asStateFlow()

    private val _pointInfo = MutableStateFlow<PointInfo?>(null)
    val pointInfo: StateFlow<PointInfo?> = _pointInfo.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _gpxTracks = MutableStateFlow<List<GpxTrack>>(emptyList())
    val gpxTracks: StateFlow<List<GpxTrack>> = _gpxTracks.asStateFlow()

    private val _selectedGpxTrack = MutableStateFlow<GpxTrack?>(null)
    val selectedGpxTrack: StateFlow<GpxTrack?> = _selectedGpxTrack.asStateFlow()

    private val _showDisclaimer = MutableStateFlow(false)
    val showDisclaimer: StateFlow<Boolean> = _showDisclaimer.asStateFlow()

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
        checkDisclaimer()
    }

    private fun checkDisclaimer() {
        if (!repository.isDisclaimerAccepted()) {
            _showDisclaimer.value = true
        }
    }

    fun acceptDisclaimer() {
        repository.setDisclaimerAccepted(true)
        _showDisclaimer.value = false
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
                _error.emit(null)
                val regionsResponse = repository.getRegions()
                repository.persistRegions(regionsResponse)
                _regions.value = regionsResponse

                val avalancheResponse = repository.getAvalancheData()
                repository.persistAvalancheData(avalancheResponse)
                _avalancheData.value = avalancheResponse.bulletins
                Log.d("MainViewModel", "Data loaded")
                calculateRules()
            } catch (e: Exception) {
                if (_avalancheData.value.isEmpty()) {
                    _error.emit(e.message)
                    Log.e("MainViewModel", "Failed to load data: ${e.message}")
                }
            }
        }
    }

    fun loadGpxTracks() {
        viewModelScope.launch(ioDispatcher) {
            val tracks = gpxRepository.getAllTracks()
            _gpxTracks.value = tracks
        }
    }

    fun importGpx(inputStream: InputStream) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val tracks = gpxParser.parse(inputStream)
                tracks.forEach { gpxRepository.saveTrack(it) }
                loadGpxTracks()
            } catch (e: Exception) {
                _error.emit("Failed to import GPX: ${e.message}")
            }
        }
    }

    fun deleteGpx(trackId: String) {
        viewModelScope.launch(ioDispatcher) {
            gpxRepository.deleteTrack(trackId)
            if (_selectedGpxTrack.value?.id == trackId) {
                _selectedGpxTrack.value = null
            }
            loadGpxTracks()
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
        val currentMode = _visualizationMode.value
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
         val bulletins = _avalancheData.value
         if (bulletins.isEmpty()) return
         val regions = _regions.value ?: return
         val mode = _visualizationMode.value
         val customParams = _customModeParams.value

         calculationJob?.cancel()
         calculationJob = viewModelScope.launch(defaultDispatcher) {
             if (mode == VisualizationMode.OFF) {
                 _generationRules.value = emptyList()
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

                 _generationRules.value = rules
             } else {
                 val bands = AvalancheLogic.processRegionElevations(bulletins)
                 val rules = AvalancheLogic.generateRules(
                     bands,
                     regions.features,
                     mode
                 )
                 _generationRules.value = rules
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
            val bulletins = _avalancheData.value
            
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

            _pointInfo.value = PointInfo(elevation, metrics.slope, metrics.aspect, dangerRatings, problems, avalancheActivity)
        }
    }

    private fun getDangerNumericValue(dangerLevel: String): Int {
        return AvalancheConfig.DANGER_LEVEL_VALUES[dangerLevel] ?: 0
    }

    fun clearPointInfo() {
        _pointInfo.value = null
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
