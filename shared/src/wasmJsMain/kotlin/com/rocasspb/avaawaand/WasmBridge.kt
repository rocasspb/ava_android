package com.rocasspb.avaawaand

import com.rocasspb.avaawaand.logic.RasterGenerator
import com.rocasspb.avaawaand.utils.GeometryUtils
import com.rocasspb.avaawaand.logic.TerrainUtils
import com.rocasspb.avaawaand.logic.ElevationProvider
import com.rocasspb.avaawaand.logic.GenerationRule
import com.rocasspb.avaawaand.utils.AvalancheConfig

@OptIn(ExperimentalJsExport::class)
@JsExport
fun isPointInPolygonWasm(lng: Double, lat: Double, ringsJson: String): Boolean {
    val point = GeometryUtils.Point(lng, lat)
    val rings: List<List<List<Double>>> = AvalancheConfig.json.decodeFromString(ringsJson)
    return GeometryUtils.isPointInPolygon(point, rings)
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun calculateTerrainMetricsWasm(lng: Double, lat: Double, elevationProviderJs: (Double, Double) -> Int): String {
    val point = GeometryUtils.Point(lng, lat)
    val metrics = TerrainUtils.calculateTerrainMetrics(point) { p ->
        val elev = elevationProviderJs(p.x, p.y)
        if (elev == -1000000) null else elev // Use a magic number for null if needed
    }
    return metrics?.let { "${it.slope},${it.aspect}" } ?: ""
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun generateRasterWasm(
    rulesJson: String,
    minLng: Double, maxLng: Double, minLat: Double, maxLat: Double,
    elevationProviderJs: (Double, Double) -> Int
): String {
    val rules: List<GenerationRule> = AvalancheConfig.json.decodeFromString(rulesJson)
    val bounds = GeometryUtils.Bounds(minLng, maxLng, minLat, maxLat)
    
    val provider = object : ElevationProvider {
        override fun getElevation(point: GeometryUtils.Point): Int? {
            val elev = elevationProviderJs(point.x, point.y)
            return if (elev == -1000000) null else elev
        }
    }
    
    val result = RasterGenerator.generateRaster(rules, bounds, provider)
    return result?.let {
        val sb = StringBuilder()
        sb.append(it.width).append(",").append(it.height)
        for (pixel in it.pixels) {
            sb.append(",").append(pixel)
        }
        sb.toString()
    } ?: "ERROR"
}
