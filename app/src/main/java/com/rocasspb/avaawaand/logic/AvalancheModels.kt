package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.data.AvalancheProblem
import com.rocasspb.avaawaand.data.Geometry
import com.rocasspb.avaawaand.utils.GeometryUtils

data class GenerationRule(
    val bounds: GeometryUtils.Bounds,
    val geometry: Geometry?,
    val minElev: Double,
    val maxElev: Double,
    val minSlope: Double? = null,
    val applySteepnessLogic: Boolean = false,
    val validAspects: List<String>? = null,
    val color: String,
    val properties: RuleProperties
)

data class RuleProperties(
    val regionId: String? = null,
    val dangerLevel: String? = null,
    val steepness: String? = null,
    val avalancheProblems: List<AvalancheProblem>? = null,
    val bulletinText: String? = null
)

data class ElevationBand(
    val regionID: String,
    val dangerLevel: String,
    val minElev: Double,
    val maxElev: Double,
    val validAspects: List<String>? = null,
    val avalancheProblems: List<AvalancheProblem> = emptyList(),
    val bulletinText: String = ""
)
