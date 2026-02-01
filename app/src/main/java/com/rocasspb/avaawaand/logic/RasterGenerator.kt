package com.rocasspb.avaawaand.logic

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.createBitmap

interface ElevationProvider {
    fun getElevation(point: GeometryUtils.Point): Int?
}

object RasterGenerator {
    private const val TAG = "RasterGenerator"
    private var totalTime = 0L
    private var callCount = 0
    private var maxTime = 0L
    private var minTime = Long.MAX_VALUE

    fun drawToBitmap(
        rules: List<GenerationRule>,
        bounds: GeometryUtils.Bounds, // Map bounds
        elevationProvider: ElevationProvider
    ): Bitmap? {
        val startTime = System.currentTimeMillis()
        
        val north = bounds.maxLat
        val south = bounds.minLat
        val east = bounds.maxLng
        val west = bounds.minLng

        val latRange = north - south
        val lngRange = east - west

        if (latRange <= 0 || lngRange <= 0) return null

        val gridSpacingDegLat = latRange / AvalancheConfig.GRID_POINTS_DENSITY.toDouble()
        val gridSpacingDegLon = lngRange / AvalancheConfig.GRID_POINTS_DENSITY.toDouble()

        val width = AvalancheConfig.GRID_POINTS_DENSITY
        val height = AvalancheConfig.GRID_POINTS_DENSITY
        
        val bitmap = createBitmap(width, height)
        val pixels = IntArray(width * height)
        val elevationCache = mutableMapOf<GeometryUtils.Point, Int?>()
        fun getElev(p: GeometryUtils.Point): Int? {
            return elevationCache.getOrPut(p) { elevationProvider.getElevation(p) }
        }

        val highColor = parseColor(AvalancheConfig.DANGER_COLORS[4] ?: "#FF0000")
        val considerableColor = parseColor(AvalancheConfig.DANGER_COLORS[3] ?: "#FF9900")

        val visibleRules = rules.filter { rule ->
            val rNorth = min(north, rule.bounds.maxLat)
            val rSouth = max(south, rule.bounds.minLat)
            val rEast = min(east, rule.bounds.maxLng)
            val rWest = max(west, rule.bounds.minLng)
            rNorth > rSouth && rEast > rWest
        }
        val ruleBaseColors = visibleRules.map { parseColor(it.color) }
        val ruleDlValues = visibleRules.map { getDangerValue(it.properties.dangerLevel) }

        for (y in 0 until width) {
            for (x in 0 until height) {
                val lng = west + (x + 0.5) * gridSpacingDegLon
                val lat = north - (y + 0.5) * gridSpacingDegLat
                val point = GeometryUtils.Point(lng, lat)

                var pixelColor = Color.TRANSPARENT
                var elevation: Int? = null
                var elevationQueried = false
                var metrics: TerrainUtils.TerrainMetrics? = null
                var metricsCalculated = false

                for (i in visibleRules.indices) {
                    val rule = visibleRules[i]
                    val baseColor = ruleBaseColors[i]
                    val dlValue = ruleDlValues[i]

                    if (lat < rule.bounds.minLat || lat > rule.bounds.maxLat ||
                        lng < rule.bounds.minLng || lng > rule.bounds.maxLng
                    ) continue

                    if (rule.geometry != null && !GeometryUtils.isPointInGeometry(point, rule.geometry)) {
                        continue
                    }

                    if (!elevationQueried) {
                        elevation = getElev(point)
                        elevationQueried = true
                    }
                    if (elevation == null || elevation < rule.minElev || elevation > rule.maxElev) {
                        continue
                    }

                    val validAspects = rule.validAspects
                    val checkAspect = !validAspects.isNullOrEmpty()
                    val checkSlope = (rule.minSlope != null && rule.minSlope > 0) || rule.applySteepnessLogic
                    var effectiveDlValue = dlValue
                    var slope: Double? = null

                    if (checkAspect || checkSlope) {
                        if (!metricsCalculated) {
                            metrics = TerrainUtils.calculateTerrainMetrics(point) { p -> getElev(p) }
                            metricsCalculated = true
                        }

                        if (metrics != null) {
                            slope = metrics.slope
                            if (checkSlope && rule.minSlope != null && slope < rule.minSlope) {
                                continue
                            }
                            if (checkAspect && !validAspects.contains(metrics.aspect)) {
                                if (dlValue <= 1) continue
                                else effectiveDlValue--
                            }
                        } else {
                            continue
                        }
                    }

                    var finalColor = baseColor
                    if (rule.applySteepnessLogic && slope != null) {
                        if (slope > 50) continue

                        var matched = true
                        if (effectiveDlValue >= 4) {
                            finalColor = if (slope >= 30) highColor else considerableColor
                        } else if (effectiveDlValue == 3) {
                            if (slope >= 35) finalColor = highColor
                            else if (slope >= 30) finalColor = considerableColor
                            else matched = false
                        } else if (effectiveDlValue == 2) {
                            if (slope >= 40) finalColor = highColor
                            else if (slope >= 35) finalColor = considerableColor
                            else matched = false
                        } else if (effectiveDlValue == 1) {
                            if (slope >= 40) finalColor = considerableColor
                            else matched = false
                        }
                        if (!matched) continue
                    }
                    pixelColor = finalColor
                }
                pixels[y * width + x] = pixelColor
            }
        }
        
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        val duration = System.currentTimeMillis() - startTime
        updateStats(duration)
        
        return bitmap
    }

    private fun updateStats(duration: Long) {
        synchronized(this) {
            callCount++
            totalTime += duration
            if (duration > maxTime) maxTime = duration
            if (duration < minTime) minTime = duration
            
            val avg = totalTime / callCount
            Log.d(TAG, "drawToBitmap: took ${duration}ms. Stats: count=$callCount, avg=${avg}ms, min=${minTime}ms, max=${maxTime}ms")
        }
    }
    
    private fun parseColor(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (_: Exception) {
            Color.TRANSPARENT
        }
    }
    
    private fun getDangerValue(level: String?): Int {
        if (level == null) return 0
        return AvalancheConfig.DANGER_LEVEL_VALUES[level] ?: 0
    }
}
