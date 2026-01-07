package com.rocasspb.avaawaand.logic

import com.rocasspb.avaawaand.data.AvalancheData
import com.rocasspb.avaawaand.data.AvalancheProblem
import com.rocasspb.avaawaand.data.DangerRating
import com.rocasspb.avaawaand.data.RegionFeature
import com.rocasspb.avaawaand.utils.AvalancheConfig
import com.rocasspb.avaawaand.utils.GeometryUtils
import kotlin.math.max
import kotlin.math.min

object AvalancheLogic {

    fun processRegionElevations(data: List<AvalancheData>): List<ElevationBand> {
        val bands = mutableListOf<ElevationBand>()

        data.forEach { bulletin ->
            var bulletinText = ""
            if (bulletin.avalancheActivity != null) {
                val parts = mutableListOf<String>()
                bulletin.avalancheActivity.highlights?.let { parts.add(it) }
                bulletin.avalancheActivity.comment?.let { parts.add(it) }
                bulletinText = parts.joinToString("\n\n")
            }

            // Using the first danger rating set if available, assuming structure applies to regions
            val dangerRatings = bulletin.dangerRatings

            if (!dangerRatings.isNullOrEmpty()) {
                bulletin.regions.forEach { region ->
                    val problems = bulletin.avalancheProblems ?: emptyList()

                    if (problems.isNotEmpty()) {
                        dangerRatings.forEach { rating ->
                            var rMin = parseElevation(rating.elevation?.lowerBound)
                            var rMax = parseElevation(rating.elevation?.upperBound, true)
                            
                            // Find matching problems
                            val matchingProblems = problems.filter { p ->
                                val pMin = parseElevation(p.elevation?.lowerBound)
                                val pMax = parseElevation(p.elevation?.upperBound, true)
                                // Overlap check
                                (rMin < pMax && rMax > pMin)
                            }
                            
                            val aspects = mutableSetOf<String>()
                            if (matchingProblems.isNotEmpty()) {
                                var unionMin = rMax
                                var unionMax = rMin
                                matchingProblems.forEach { p ->
                                    p.aspects?.let { aspects.addAll(it) }
                                    val pMin = parseElevation(p.elevation?.lowerBound)
                                    val pMax = parseElevation(p.elevation?.upperBound, true)
                                    unionMin = min(unionMin, pMin)
                                    unionMax = max(unionMax, pMax)
                                }
                                rMin = unionMin
                                rMax = unionMax
                            }
                            
                            bands.add(ElevationBand(
                                regionID = region.id,
                                dangerLevel = rating.mainValue,
                                minElev = rMin,
                                maxElev = rMax,
                                validAspects = if (aspects.isNotEmpty()) aspects.toList() else null,
                                avalancheProblems = matchingProblems,
                                bulletinText = bulletinText
                            ))
                        }
                    } else {
                        // Fallback
                        val maxDanger = getMaxDanger(dangerRatings)
                        if (maxDanger != null) {
                             bands.add(ElevationBand(
                                regionID = region.id,
                                dangerLevel = maxDanger.mainValue,
                                minElev = 0.0,
                                maxElev = AvalancheConfig.DEFAULT_MAX_ELEVATION,
                                validAspects = null,
                                avalancheProblems = emptyList(),
                                bulletinText = bulletinText
                            ))
                        }
                    }
                }
            }
        }
        return bands
    }

    private fun parseElevation(value: String?, isMax: Boolean = false): Double {
        if (value == null) return if (isMax) AvalancheConfig.DEFAULT_MAX_ELEVATION else 0.0
        val v = value.toDoubleOrNull()
        if (v != null) return v
        //TODO  Handle "treeline" etc if needed, though usually handled in adjustElevationForTreeline later or here?
        return if (isMax) AvalancheConfig.DEFAULT_MAX_ELEVATION else 0.0
    }

    private fun getMaxDanger(ratings: List<DangerRating>): DangerRating? {
        if (ratings.isEmpty()) return null
        var maxRating = ratings[0]
        var maxVal = AvalancheConfig.DANGER_LEVEL_VALUES[maxRating.mainValue] ?: 0
        
        for (r in ratings) {
            val v = AvalancheConfig.DANGER_LEVEL_VALUES[r.mainValue] ?: 0
            if (v > maxVal) {
                maxVal = v
                maxRating = r
            }
        }
        return maxRating
    }
    
    fun adjustElevationForTreeline(currentMin: Double, currentMax: Double, problems: List<AvalancheProblem>): Pair<Double, Double> {
        var min = currentMin
        var max = currentMax
        
        if (problems.isNotEmpty()) {
            problems.forEach { p ->
                val lb = p.elevation?.lowerBound?.lowercase()
                val ub = p.elevation?.upperBound?.lowercase()
                
                if (lb == "treeline") {
                    min = max(min, AvalancheConfig.TREELINE_ELEVATION)
                }
                if (ub == "treeline") {
                    max = min(max, AvalancheConfig.TREELINE_ELEVATION)
                }
            }
        }
        return Pair(min, max)
    }

    fun generateRules(
        bands: List<ElevationBand>, 
        regionFeatures: List<RegionFeature>,
        visualizationMode: VisualizationMode
    ): List<GenerationRule> {
        val rules = mutableListOf<GenerationRule>()
        val regionsMap = regionFeatures.associateBy { it.properties.id }

        for (band in bands) {
            val feature = regionsMap[band.regionID] ?: continue
            val bounds = GeometryUtils.getBounds(feature)
            val color = AvalancheConfig.DANGER_COLORS[AvalancheConfig.DANGER_LEVEL_VALUES[band.dangerLevel]] ?: "0xFFFFFF"
            
            val useAspectAndElevation = visualizationMode == VisualizationMode.RISK
            val (ruleMin, ruleMax) = adjustElevationForTreeline(band.minElev, band.maxElev, band.avalancheProblems)
            
            rules.add(GenerationRule(
                bounds = bounds,
                geometry = feature.geometry,
                minElev = ruleMin,
                maxElev = ruleMax,
                minSlope = if (visualizationMode == VisualizationMode.BULLETIN) null else 30.0,
                applySteepnessLogic = useAspectAndElevation,
                validAspects = if (useAspectAndElevation) band.validAspects else null,
                color = color,
                properties = RuleProperties(
                    regionId = band.regionID,
                    dangerLevel = band.dangerLevel,
                    avalancheProblems = band.avalancheProblems,
                    bulletinText = band.bulletinText
                )
            ))
        }
        return rules
    }
}
