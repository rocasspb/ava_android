package com.rocasspb.avaawaand.utils

import com.rocasspb.avaawaand.data.Geometry
import com.rocasspb.avaawaand.data.RegionFeature
import kotlin.math.*

object GeometryUtils {

    data class Point(val x: Double, val y: Double)
    data class Bounds(val minLng: Double, val maxLng: Double, val minLat: Double, val maxLat: Double)

    /**
     * Checks if a point is inside a polygon using the Ray Casting algorithm.
     * @param point The point to check (x=lng, y=lat)
     * @param rings The polygon coordinates (List of rings, where each ring is a List of coordinates [lng, lat])
     */
    fun isPointInPolygon(point: Point, rings: List<List<List<Double>>>): Boolean {
        var inside = false
        val x = point.x
        val y = point.y

        for (ring in rings) {
            var j = ring.size - 1
            for (i in ring.indices) {
                val xi = ring[i][0]
                val yi = ring[i][1]
                val xj = ring[j][0]
                val yj = ring[j][1]

                val intersect = ((yi > y) != (yj > y)) &&
                        (x < (xj - xi) * (y - yi) / (yj - yi) + xi)
                if (intersect) {
                    inside = !inside
                }
                j = i
            }
        }
        return inside
    }

    /**
     * Checks if a point is inside a geometry (Polygon or MultiPolygon).
     */
    fun isPointInGeometry(point: Point, geometry: Geometry): Boolean {
        return when (geometry.type) {
            "Polygon" -> {
                if (geometry.coordinates.isNotEmpty()) {
                    isPointInMultiPolygon(point, geometry.coordinates)
                } else false
            }
            "MultiPolygon" -> {
                isPointInMultiPolygon(point, geometry.coordinates)
            }
            else -> false
        }
    }

    private fun isPointInMultiPolygon(point: Point, polygons: List<List<List<List<Double>>>>): Boolean {
        for (polygon in polygons) {
            if (isPointInPolygon(point, polygon)) {
                return true
            }
        }
        return false
    }

    /**
     * Calculates the distance between two points (lat1, lon1) and (lat2, lon2) in meters
     * using the Haversine formula.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun getBounds(feature: RegionFeature): Bounds {
        var minLng = 180.0
        var maxLng = -180.0
        var minLat = 90.0
        var maxLat = -90.0

        fun processRing(ring: List<List<Double>>) {
            for (coord in ring) {
                if (coord.size >= 2) {
                    val lng = coord[0]
                    val lat = coord[1]
                    minLng = min(minLng, lng)
                    maxLng = max(maxLng, lng)
                    minLat = min(minLat, lat)
                    maxLat = max(maxLat, lat)
                }
            }
        }

        val geometry = feature.geometry
        if (geometry.coordinates.isNotEmpty()) {
            for (polygon in geometry.coordinates) {
                for (ring in polygon) {
                    processRing(ring)
                }
            }
        }

        return Bounds(minLng, maxLng, minLat, maxLat)
    }
}
