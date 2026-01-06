package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.*

object TerrainUtils {

    // Offset in degrees used for slope calculation
    // From sample: 0.0001 degrees
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
        queryElevation: (GeometryUtils.Point) -> Double?
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
        val distX = 2 * offset * METERS_PER_DEGREE * cos(Math.toRadians(lat))

        val dzDx = (zE - zW) / distX
        val dzDy = (zN - zS) / distY

        // Slope
        val slopeRad = atan(sqrt(dzDx * dzDx + dzDy * dzDy))
        val slopeDeg = Math.toDegrees(slopeRad)

        // Aspect
        val downhillX = -dzDx
        val downhillY = -dzDy
        val angleFromEastCCW = Math.toDegrees(atan2(downhillY, downhillX))

        var bearing = 90 - angleFromEastCCW
        if (bearing < 0) bearing += 360

        val aspect = when {
            bearing !in 22.5..<337.5 -> "N"
            bearing in 22.5..<67.5 -> "NE"
            bearing in 112.5..<157.5 -> "SE"
            bearing in 157.5..<202.5 -> "S"
            bearing in 202.5..<247.5 -> "SW"
            bearing in 247.5..<292.5 -> "W"
            bearing in 292.5..<337.5 -> "NW"
            else -> ""
        }

        return TerrainMetrics(slopeDeg, aspect)
    }
}
