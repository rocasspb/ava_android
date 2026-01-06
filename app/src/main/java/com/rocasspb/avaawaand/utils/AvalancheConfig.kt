package com.rocasspb.avaawaand.utils

object AvalancheConfig {
    const val DEFAULT_MAX_ELEVATION = 4000.0
    const val TREELINE_ELEVATION = 2000.0
    const val GRID_POINTS_DENSITY = 100 // Adjust as needed
    
    val DANGER_COLORS = mapOf(
        "1" to "#CCFF66",
        "2" to "#FFFF00",
        "3" to "#FF9900",
        "4" to "#FF0000",
        "5" to "#330000",
        "default" to "#FFFFFF",
        "high" to "#FF0000",
        "considerable" to "#FF9900"
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
}
