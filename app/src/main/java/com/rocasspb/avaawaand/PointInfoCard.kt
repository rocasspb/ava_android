package com.rocasspb.avaawaand

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun PointInfoCard(pointInfo: MainViewModel.PointInfo, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val maxHeight = if (isLandscape) {
        configuration.screenHeightDp.dp - 32.dp // Full height minus padding
    } else {
        configuration.screenHeightDp.dp / 2
    }

    Card(
        modifier = modifier
            .widthIn(max = 280.dp)
            .heightIn(max = maxHeight),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Terrain Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoItem(label = "Elev", value = "${pointInfo.elevation}m")
                InfoItem(label = "Slope", value = String.format(Locale.US, "%.1f°", pointInfo.slope))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Aspect", color = Color.Gray, fontSize = 10.sp)
                    WindRose(
                        selectedAspects = setOf(pointInfo.aspect),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            if (pointInfo.dangerRatings != null || pointInfo.avalancheProblems != null || pointInfo.avalancheActivity != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                pointInfo.dangerRatings?.forEach { rating ->
                    DangerItem(rating)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                pointInfo.avalancheProblems?.let { problems ->
                    if (problems.isNotEmpty()) {
                        if (pointInfo.dangerRatings != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        problems.forEach { problem ->
                            ProblemItem(problem)
                        }
                    }
                }

                // Avalanche Activity
                pointInfo.avalancheActivity?.let { activity ->
                    if (activity.highlights != null || activity.comment != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = buildAnnotatedString {
                                activity.highlights?.let {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                                        append(formatHtmlText(it))
                                    }
                                    if (activity.comment != null) append("\n\n")
                                }
                                activity.comment?.let {
                                    withStyle(style = SpanStyle(color = Color.DarkGray)) {
                                        append(formatHtmlText(it))
                                    }
                                }
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DangerItem(rating: com.rocasspb.avaawaand.data.DangerRating) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = getDangerIcon(rating.mainValue)),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = rating.mainValue.replaceFirstChar { it.uppercase() },
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            ElevationLabel(rating.elevation)
        }
    }
}

@Composable
private fun ElevationLabel(elevation: com.rocasspb.avaawaand.data.Elevation?) {
    elevation?.let { elev ->
        val label = formatElevationRange(elev.lowerBound, elev.upperBound)
        if (label != null) {
            Text(text = label, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, fontSize = 10.sp)
        Text(text = value, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProblemItem(problem: com.rocasspb.avaawaand.data.AvalancheProblem) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(id = getProblemIcon(problem.problemType)),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = problem.problemType.replace("_", " ").replaceFirstChar { it.uppercase() },
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            // Aspects Wind Rose
            problem.aspects?.let { aspects ->
                if (aspects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    WindRose(
                        selectedAspects = aspects.toSet(),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
            
            // Details
            val details = mutableListOf<String>()
            
            // Elevation
            val elevLabel = formatElevationRange(problem.elevation?.lowerBound, problem.elevation?.upperBound)
            elevLabel?.let { details.add("Elev: $it") }

            // Stability, Frequency, Size
            problem.snowpackStability?.let { details.add("Stability: $it") }
            problem.frequency?.let { details.add("Freq: $it") }
            problem.avalancheSize?.let { details.add("Size: $it") }

            if (details.isNotEmpty()) {
                Text(
                    text = details.joinToString("\n"),
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}

private fun formatHtmlText(text: String): String {
    return text
        .replace("<br/>", "\n")
        .replace("<br>", "\n")
        .replace("</br>", "")
        .replace("&nbsp;", " ")
        .trim()
}

private fun formatElevationRange(lower: String?, upper: String?): String? {
    fun formatValue(v: String?): String? {
        if (v == null) return null
        return if (v.lowercase() == "treeline") "Treeline" else "${v}m"
    }
    val l = formatValue(lower)
    val u = formatValue(upper)
    return when {
        l != null && u != null -> "$l - $u"
        l != null -> "> $l"
        u != null -> "< $u"
        else -> null
    }
}

private fun getDangerIcon(dangerLevel: String): Int {
    return when (dangerLevel.lowercase()) {
        "low", "1" -> R.drawable.ic_danger_1
        "moderate", "2" -> R.drawable.ic_danger_2
        "considerable", "3" -> R.drawable.ic_danger_3
        "high", "4" -> R.drawable.ic_danger_4
        "very_high", "5" -> R.drawable.ic_danger_5
        else -> R.drawable.ic_bulletin
    }
}

private fun getProblemIcon(problemType: String): Int {
    return when (problemType.lowercase()) {
        "new_snow" -> R.drawable.ic_prob_new_snow
        "wind_slab" -> R.drawable.ic_prob_wind_slab
        "persistent_weak_layers" -> R.drawable.ic_prob_persistent_weak_layers
        "gliding_snow" -> R.drawable.ic_prob_gliding_snow
        "wet_snow" -> R.drawable.ic_prob_wet_snow
        else -> R.drawable.ic_bulletin
    }
}
