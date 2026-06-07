package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.*

object TerrainUtils {
    private const val SLOPE_CALCULATION_OFFSET = 0.0001
    private const val METERS_PER_DEGREE = 111111.0

    data class TerrainMetrics(val slope: Double, val aspect: String)

    /**
     * Calculates slope and aspect for a given point.
     * @param point The point [lng, lat]
     * @param queryElevation Function to get elevation at a point
     */
    fun calculateTerrainMetrics(
        point: GeometryUtils.Point,
        queryElevation: (GeometryUtils.Point) -> Int?
    ): TerrainMetrics? {
        val lng = point.x
        val lat = point.y
        val offset = SLOPE_CALCULATION_OFFSET

        val zN = queryElevation(GeometryUtils.Point(lng, lat + offset))
        val zE = queryElevation(GeometryUtils.Point(lng + offset, lat))
        val zS = queryElevation(GeometryUtils.Point(lng, lat - offset))
        val zW = queryElevation(GeometryUtils.Point(lng - offset, lat))

        if (zN == null || zE == null || zS == null || zW == null) return null

        val distY = 2 * offset * METERS_PER_DEGREE
        val distX = 2 * offset * METERS_PER_DEGREE * cos(lat * PI / 180.0)

        val dzDx = (zE - zW) / distX
        val dzDy = (zN - zS) / distY

        // Slope
        val slopeRad = atan(sqrt(dzDx * dzDx + dzDy * dzDy))
        val slopeDeg = slopeRad * 180.0 / PI

        // Aspect
        val downhillX = -dzDx
        val downhillY = -dzDy
        val angleFromEastCCW = atan2(downhillY, downhillX) * 180.0 / PI

        var bearing = 90 - angleFromEastCCW
        if (bearing < 0) bearing += 360

        val aspect = when {
            bearing < 22.5 || bearing >= 337.5 -> "N"
            bearing < 67.5 -> "NE"
            bearing < 112.5 -> "E"
            bearing < 157.5 -> "SE"
            bearing < 202.5 -> "S"
            bearing < 247.5 -> "SW"
            bearing < 292.5 -> "W"
            bearing < 337.5 -> "NW"
            else -> ""
        }

        return TerrainMetrics(slopeDeg, aspect)
    }
}
