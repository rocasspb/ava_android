package com.rocasspb.avaawaand.utils

object AvalancheConfig {
    const val DEFAULT_MAX_ELEVATION = 4000
    const val TREELINE_ELEVATION = 2000
    const val GRID_POINTS_DENSITY = 100 // Adjust as needed
    
    val DANGER_COLORS = mapOf(
        1 to "#CCFF66",
        2 to "#FFFF00",
        3 to "#FF9900",
        4 to "#FF0000",
        5 to "#330000",
    )
    
    val DANGER_LEVEL_VALUES = mapOf(
        "1" to 1,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "low" to 1,
        "moderate" to 2,
        "considerable" to 3,
        "high" to 4,
        "very_high" to 5
    )

    data class SteepnessThreshold(
        val minSlope: Int,
        val color: String,
        val label: String
    )

    val STEEPNESS_THRESHOLDS = listOf(
        SteepnessThreshold(30, "#FFFF33", "> 30°"),
        SteepnessThreshold(35, "#FF9900", "> 35°"),
        SteepnessThreshold(40, "#FF0000", "> 40°")
    )

    val EUREGIO_BOUNDS = GeometryUtils.Bounds(
        minLng = -10.0,
        maxLng = 20.0,
        minLat = 35.0,
        maxLat = 60.0
    )

}
