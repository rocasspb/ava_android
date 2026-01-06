package com.rocasspb.avaawaand.utils

import com.rocasspb.avaawaand.data.Geometry
import com.rocasspb.avaawaand.data.RegionFeature
import kotlin.math.max
import kotlin.math.min

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

        // We treat the first ring as outer and others as holes, but for simple ray casting
        // on the whole set of segments, the even-odd rule handles holes correctly implicitly.
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
     * This adapts to the specific weirdness of the Region data class if necessary,
     * but strictly speaking, we need to handle the List nesting.
     *
     * Based on Region.kt: coordinates is List<List<List<List<Double>>>>
     * This structure suggests MultiPolygon: [Polygon1, Polygon2...]
     * Where Polygon is [Ring1, Ring2...]
     * Where Ring is [[x,y], [x,y]...]
     *
     * If the type is Polygon, it might be nested one level deeper or the data class is rigid.
     * We will try to handle based on type string.
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
            // Treat everything as list of polygons
            for (polygon in geometry.coordinates) {
                for (ring in polygon) {
                    processRing(ring)
                }
            }
        }

        return Bounds(minLng, maxLng, minLat, maxLat)
    }
}
