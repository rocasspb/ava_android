package com.rocasspb.avaawaand.logic

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.ceil
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt

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

        val width = if(lngRange > latRange) AvalancheConfig.GRID_POINTS_DENSITY else ceil(AvalancheConfig.GRID_POINTS_DENSITY * lngRange / latRange).toInt()
        val height = if(lngRange < latRange) AvalancheConfig.GRID_POINTS_DENSITY else ceil(AvalancheConfig.GRID_POINTS_DENSITY * latRange / lngRange).toInt()

        val gridSpacingDegLat = latRange / height
        val gridSpacingDegLon = lngRange / width

        val bitmap = createBitmap(width, height)
        val pixels = IntArray(width * height)
        val elevationCache = mutableMapOf<GeometryUtils.Point, Int?>()
        fun getElev(p: GeometryUtils.Point): Int? {
            return elevationCache.getOrPut(p) { elevationProvider.getElevation(p) }
        }

        val highColor = parseColor(AvalancheConfig.DANGER_COLORS[4] ?: "#FF0000")
        val considerableColor = parseColor(AvalancheConfig.DANGER_COLORS[3] ?: "#FF9900")

        for (y in 0 until height) {
            for (x in 0 until width) {
                val lng = west + (x + 0.5) * gridSpacingDegLon
                val lat = north - (y + 0.5) * gridSpacingDegLat
                val point = GeometryUtils.Point(lng, lat)

                val elevation: Int? = getElev(point)
                var pixelColor = Color.TRANSPARENT

                for (rule in rules) {
                    if (lat < rule.bounds.minLat || lat > rule.bounds.maxLat ||
                        lng < rule.bounds.minLng || lng > rule.bounds.maxLng
                    ) continue

                    if (elevation == null || elevation < rule.minElev || elevation > rule.maxElev) {
                        continue
                    }

                    val validAspects = rule.validAspects
                    val checkAspect = !validAspects.isNullOrEmpty()
                    val checkSlope = (rule.minSlope != null && rule.minSlope > 0) || rule.applySteepnessLogic
                    val dlValue = getDangerValue(rule.properties.dangerLevel)
                    var effectiveDlValue = dlValue
                    var slope: Double? = null

                    if (checkAspect || checkSlope) {
                        val metrics = TerrainUtils.calculateTerrainMetrics(point) { p -> getElev(p) }
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

                    var finalColor = parseColor(rule.color)
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
        callCount++
        totalTime += duration
        if (duration > maxTime) maxTime = duration
        if (duration < minTime) minTime = duration

        val avg = totalTime / callCount
        Log.d(TAG, "drawToBitmap: took ${duration}ms. Stats: count=$callCount, avg=${avg}ms, min=${minTime}ms, max=${maxTime}ms")
    }
    
    private fun parseColor(hex: String): Int {
        return try {
            hex.toColorInt()
        } catch (_: Exception) {
            Color.TRANSPARENT
        }
    }
    
    private fun getDangerValue(level: String?): Int {
        if (level == null) return 0
        return AvalancheConfig.DANGER_LEVEL_VALUES[level] ?: 0
    }
}
