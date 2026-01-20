package com.rocasspb.avaawaand.logic

import android.graphics.Bitmap
import android.graphics.Color
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import androidx.core.graphics.createBitmap

interface ElevationProvider {
    fun getElevation(point: GeometryUtils.Point): Int?
}

object RasterGenerator {
    fun drawToBitmap(
        rules: List<GenerationRule>,
        bounds: GeometryUtils.Bounds, // Map bounds
        elevationProvider: ElevationProvider
    ): Bitmap? {
        val north = bounds.maxLat
        val south = bounds.minLat
        val east = bounds.maxLng
        val west = bounds.minLng

        val latRange = north - south
        val lngRange = east - west

        if (latRange <= 0 || lngRange <= 0) return null

        val gridSpacingDeg = max(latRange, lngRange) / AvalancheConfig.GRID_POINTS_DENSITY.toDouble()

        val width = ceil(lngRange / gridSpacingDeg).toInt()
        val height = ceil(latRange / gridSpacingDeg).toInt()
        
        if (width <= 0 || height <= 0) return null
        
        // Safety cap
        val safeWidth = min(width, 2000)
        val safeHeight = min(height, 2000)

        val bitmap = createBitmap(safeWidth, safeHeight)
        val pixels = IntArray(safeWidth * safeHeight)
        val elevationCache = mutableMapOf<String, Int?>()
        fun getElev(p: GeometryUtils.Point): Int? {
            val key = "${p.x},${p.y}"
            return elevationCache.getOrPut(key) { elevationProvider.getElevation(p) }
        }

        for (rule in rules) {
            val rNorth = min(north, rule.bounds.maxLat)
            val rSouth = max(south, rule.bounds.minLat)
            val rEast = min(east, rule.bounds.maxLng)
            val rWest = max(west, rule.bounds.minLng)

            if (rNorth <= rSouth || rEast <= rWest) continue

            val startX = floor((rWest - west) / gridSpacingDeg).toInt()
            val endX = ceil((rEast - west) / gridSpacingDeg).toInt()
            val startY = floor((north - rNorth) / gridSpacingDeg).toInt()
            val endY = ceil((north - rSouth) / gridSpacingDeg).toInt()

            val sX = max(0, startX)
            val eX = min(safeWidth, endX)
            val sY = max(0, startY)
            val eY = min(safeHeight, endY)

            val baseColor = parseColor(rule.color)

            for (x in sX until eX) {
                for (y in sY until eY) {
                    val lng = west + (x + 0.5) * gridSpacingDeg
                    val lat = north - (y + 0.5) * gridSpacingDeg
                    val point = GeometryUtils.Point(lng, lat)

                    if (rule.geometry != null) {
                        if (!GeometryUtils.isPointInGeometry(point, rule.geometry)) {
                            continue
                        }
                    }

                    val elevation = getElev(point)
                    if (elevation != null && elevation >= rule.minElev && elevation <= rule.maxElev) {
                        var slope: Double? = null
                        val dlValue = getDangerValue(rule.properties.dangerLevel)

                        val checkAspect = !rule.validAspects.isNullOrEmpty()
                        val checkSlope = (rule.minSlope != null && rule.minSlope > 0) || rule.applySteepnessLogic

                        var effectiveDlValue = dlValue
                        if (checkAspect || checkSlope) {
                            val metrics = TerrainUtils.calculateTerrainMetrics(point) { p -> getElev(p) }
                            if (metrics != null) {
                                slope = metrics.slope
                                if (checkSlope) {
                                    if (rule.minSlope != null && slope < rule.minSlope) {
                                        continue
                                    }
                                }
                                
                                if (checkAspect) {
                                    if (!rule.validAspects.contains(metrics.aspect)) {
                                        if (dlValue <= 1) continue
                                        else effectiveDlValue--
                                    }
                                }
                            } else {
                                continue
                            }
                        }
                        
                        var finalColor = baseColor

                        if (rule.applySteepnessLogic && slope != null) {
                            val highColor = parseColor(AvalancheConfig.DANGER_COLORS[4] ?: "#FF0000")
                            val considerableColor = parseColor(AvalancheConfig.DANGER_COLORS[3] ?: "#FF9900")
                            if(slope > 50) continue

                            if (effectiveDlValue >= 4) {
                                finalColor = if (slope >= 30) highColor else considerableColor
                            } else if (effectiveDlValue == 3) {
                                if (slope >= 35) finalColor = highColor
                                else if (slope >= 30) finalColor = considerableColor
                                else continue // Skip
                            } else if (effectiveDlValue == 2) {
                                if (slope >= 40) finalColor = highColor
                                else if (slope >= 35) finalColor = considerableColor
                                else continue // Skip
                            } else if (effectiveDlValue == 1) {
                                if (slope >= 40) finalColor = considerableColor
                                else continue // Skip
                            }
                        }

                        pixels[y * safeWidth + x] = finalColor
                    }
                }
            }
        }
        
        bitmap.setPixels(pixels, 0, safeWidth, 0, 0, safeWidth, safeHeight)
        return bitmap
    }
    
    private fun parseColor(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: Exception) {
            Color.TRANSPARENT
        }
    }
    
    private fun getDangerValue(level: String?): Int {
        if (level == null) return 0
        return AvalancheConfig.DANGER_LEVEL_VALUES[level] ?: 0
    }
}
