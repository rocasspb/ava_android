package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import com.rocasspb.avaawaand.utils.PlatformUtils
import kotlin.math.ceil

interface ElevationProvider {
    fun getElevation(point: GeometryUtils.Point): Int?
}

interface Logger {
    fun d(tag: String, message: String)
}

object RasterGenerator {
    private const val TAG = "RasterGenerator"
    private var totalTime = 0L
    private var callCount = 0
    private var maxTime = 0L
    private var minTime = Long.MAX_VALUE

    fun generateRaster(
        rules: List<GenerationRule>,
        bounds: GeometryUtils.Bounds, // Map bounds
        elevationProvider: ElevationProvider,
        logger: Logger? = null
    ): RasterData? {
        val startTime = PlatformUtils.currentTimeMillis()

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

        val pixels = IntArray(width * height)
        val elevationCache = mutableMapOf<GeometryUtils.Point, Int?>()
        fun getElev(p: GeometryUtils.Point): Int? {
            return elevationCache.getOrPut(p) { elevationProvider.getElevation(p) }
        }

        val highColor = parseHexColor(AvalancheConfig.DANGER_COLORS[4] ?: "#FF0000")
        val considerableColor = parseHexColor(AvalancheConfig.DANGER_COLORS[3] ?: "#FF9900")
        val ruleColors = rules.map { parseHexColor(it.color) }
        var coloredPixels = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val lng = west + (x + 0.5) * gridSpacingDegLon
                val lat = north - (y + 0.5) * gridSpacingDegLat
                val point = GeometryUtils.Point(lng, lat)

                val elevation: Int? = getElev(point)
                var pixelColor = 0x00000000 // Transparent

                for ((index, rule) in rules.withIndex()) {
                    if (lat < rule.bounds.minLat || lat > rule.bounds.maxLat ||
                        lng < rule.bounds.minLng || lng > rule.bounds.maxLng
                    ) continue

                    if (elevation == null || elevation < rule.minElev || elevation > rule.maxElev) {
                        continue
                    }

                    if(rule.geometry != null && !GeometryUtils.isPointInGeometry(point, rule.geometry))
                        continue

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

                    var finalColor = ruleColors[index]
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
                if (pixelColor != 0x00000000) {
                    coloredPixels++
                }
                pixels[y * width + x] = pixelColor
            }
        }

        val duration = PlatformUtils.currentTimeMillis() - startTime
        logger?.d(TAG, "generateRaster complete. Colored pixels: $coloredPixels / ${width * height}. Rules: ${rules.size}")
        updateStats(duration, logger)
        
        return RasterData(width, height, pixels)
    }

    private fun updateStats(duration: Long, logger: Logger?) {
        callCount++
        totalTime += duration
        if (duration > maxTime) maxTime = duration
        if (duration < minTime) minTime = duration

        val avg = totalTime / callCount
        logger?.d(TAG, "generateRaster: took ${duration}ms. Stats: count=$callCount, avg=${avg}ms, min=${minTime}ms, max=${maxTime}ms")
    }
    
    private fun parseHexColor(hex: String): Int {
        val cleaned = hex.removePrefix("#")
        return when (cleaned.length) {
            6 -> (0xFF shl 24) or cleaned.toInt(16)
            8 -> cleaned.toLong(16).toInt()
            else -> 0x00000000
        }
    }
    
    private fun getDangerValue(level: String?): Int {
        if (level == null) return 0
        return AvalancheConfig.DANGER_LEVEL_VALUES[level] ?: 0
    }
}
