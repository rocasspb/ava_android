package com.rocasspb.avaawaand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rocasspb.avaawaand.logic.CustomModeParams
import com.rocasspb.avaawaand.logic.VisualizationMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ModeSelectionPanel(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visualizationMode by viewModel.visualizationMode.observeAsState(VisualizationMode.BULLETIN)
    val customParams by viewModel.customModeParams.observeAsState(CustomModeParams())

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF5F6368)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModeOption(
                    isSelected = visualizationMode == VisualizationMode.BULLETIN,
                    iconRes = R.drawable.ic_bulletin,
                    label = stringResource(R.string.mode_bulletin),
                    onClick = { viewModel.setVisualizationMode(VisualizationMode.BULLETIN) }
                )
                ModeOption(
                    isSelected = visualizationMode == VisualizationMode.RISK,
                    iconRes = R.drawable.ic_landscape,
                    label = stringResource(R.string.mode_risk),
                    onClick = { viewModel.setVisualizationMode(VisualizationMode.RISK) }
                )
                ModeOption(
                    isSelected = visualizationMode == VisualizationMode.CUSTOM,
                    iconRes = R.drawable.ic_custom,
                    label = stringResource(R.string.mode_custom),
                    onClick = { viewModel.setVisualizationMode(VisualizationMode.CUSTOM) }
                )
                ModeOption(
                    isSelected = visualizationMode == VisualizationMode.OFF,
                    iconRes = R.drawable.ic_close,
                    label = stringResource(R.string.mode_off),
                    onClick = { viewModel.setVisualizationMode(VisualizationMode.OFF) }
                )
            }

            if (visualizationMode == VisualizationMode.CUSTOM) {
                CustomControls(
                    params = customParams,
                    onParamsChange = { viewModel.updateCustomParams(it) }
                )
            }
        }
    }
}

@Composable
fun ModeOption(
    isSelected: Boolean,
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    val contentColor = if (isSelected) Color(0xFF1A73E8) else Color(0xFF5F6368)
    val bgColor = if (isSelected) Color(0xFFE8F0FE) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = contentColor
            )
        }
        Text(
            text = label,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomControls(
    params: CustomModeParams,
    onParamsChange: (CustomModeParams) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.elevation_range_m),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${params.minElev}m - ${params.maxElev}m",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        RangeSlider(
            value = params.minElev.toFloat()..params.maxElev.toFloat(),
            onValueChange = { range ->
                onParamsChange(params.copy(minElev = range.start.toInt(), maxElev = range.endInclusive.toInt()))
            },
            valueRange = 0f..5000f
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.min_steepness_degrees),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${params.minSlope}°",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Slider(
            value = params.minSlope.toFloat(),
            onValueChange = { val_ ->
                onParamsChange(params.copy(minSlope = val_.toInt()))
            },
            valueRange = 30f..45f,
            steps = 14
        )

        Text(
            text = stringResource(R.string.aspects),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        val allAspects = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            allAspects.forEach { aspect ->
                FilterChip(
                    selected = params.aspects.contains(aspect),
                    onClick = {
                        val newAspects = if (params.aspects.contains(aspect)) {
                            params.aspects.filter { it != aspect }
                        } else {
                            params.aspects + aspect
                        }
                        onParamsChange(params.copy(aspects = newAspects))
                    },
                    label = { Text(aspect) }
                )
            }
        }
    }
}
