package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import com.rocasspb.avaawaand.utils.PlatformUtils
import kotlin.math.ceil
import kotlin.math.min

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
        val ruleColors = rules.associateWith { parseHexColor(it.color) }
        var coloredPixels = 0

        val filteredRules = rules.filter {
            (north > it.bounds.minLat && south < it.bounds.maxLat) &&
            (east > it.bounds.minLng && west < it.bounds.maxLng)
        }.sortedBy {
            it.properties.dangerLevel
        }

        val cellCount = 20
        val cellWidthPixels = ceil(width.toDouble() / cellCount).toInt()
        val cellHeightPixels = ceil(height.toDouble() / cellCount).toInt()

        val rulesPerCell = Array(cellCount) { cy ->
            Array(cellCount) { cx ->
                val x = cx * cellWidthPixels
                val y = cy * cellHeightPixels

                val cellMidLon = west + (x + cellWidthPixels / 2) * gridSpacingDegLon
                val cellMidLat = north - (y + cellHeightPixels / 2) * gridSpacingDegLat
                val cellMidPoint = GeometryUtils.Point(cellMidLon, cellMidLat)

                val cellTopLeftLon = west + x * gridSpacingDegLon
                val cellTopLeftLat = north - y * gridSpacingDegLat
                val cellTopLeftPoint = GeometryUtils.Point(cellTopLeftLon, cellTopLeftLat)

                val cellBottomRightLon = west + ((cx + 1) * cellWidthPixels + cellWidthPixels / 2) * gridSpacingDegLon //it's okay to intersect with the next cell
                val cellBottomRightLat = north - ((cy + 1) * cellHeightPixels + cellHeightPixels / 2) * gridSpacingDegLat
                val cellBottomRightPoint = GeometryUtils.Point(cellBottomRightLon, cellBottomRightLat)

                filteredRules.filter { it.geometry == null
                        || GeometryUtils.isPointInGeometry(cellMidPoint, it.geometry)
                        || GeometryUtils.isPointInGeometry(cellTopLeftPoint, it.geometry)
                        || GeometryUtils.isPointInGeometry(cellBottomRightPoint, it.geometry) }
            }
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val lng = west + (x + 0.5) * gridSpacingDegLon
                val lat = north - (y + 0.5) * gridSpacingDegLat
                val point = GeometryUtils.Point(lng, lat)

                val elevation: Int? = getElev(point)
                var pixelColor = 0x00000000 // Transparent

                val relevantRules = rulesPerCell[min(y / cellHeightPixels, cellCount - 1)][min(x / cellWidthPixels, cellCount - 1)]
                for (rule in relevantRules) {
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

                    var finalColor = ruleColors[rule] ?: 0x00000000
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
