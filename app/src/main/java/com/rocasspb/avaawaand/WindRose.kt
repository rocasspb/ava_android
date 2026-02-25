package com.rocasspb.avaawaand

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class AspectSector(
    val id: String,
    val centerAngle: Float,
    val label: String
)

val ASPECT_SECTORS = listOf(
    AspectSector("E", 0f, "E"),
    AspectSector("SE", 45f, "SE"),
    AspectSector("S", 90f, "S"),
    AspectSector("SW", 135f, "SW"),
    AspectSector("W", 180f, "W"),
    AspectSector("NW", 225f, "NW"),
    AspectSector("N", 270f, "N"),
    AspectSector("NE", 315f, "NE")
)

fun findSectorForAngle(angle: Float): AspectSector? {
    // Normalize angle to [0, 360)
    val normalizedAngle = (angle + 360) % 360

    // Find sector
    return ASPECT_SECTORS.find { sector ->
        val start = (sector.centerAngle - 22.5f + 360) % 360
        val end = (sector.centerAngle + 22.5f + 360) % 360
        if (start < end) {
            normalizedAngle in start..end
        } else {
            // Sector crosses 0 (East)
            normalizedAngle >= start || normalizedAngle <= end
        }
    }
}

@Composable
fun WindRose(
    selectedAspects: Set<String>,
    onAspectClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = Color.Transparent
    val strokeColor = MaterialTheme.colorScheme.outline
    val innerCircleColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = modifier
            .pointerInput(onAspectClick) {
                if (onAspectClick != null) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2
                        val centerY = size.height / 2
                        val angle = Math.toDegrees(
                            atan2(
                                (offset.y - centerY).toDouble(),
                                (offset.x - centerX).toDouble()
                            )
                        ).toFloat()

                        findSectorForAngle(angle)?.let { onAspectClick(it.id) }
                    }
                }
            }
    ) {
        val radius = size.minDimension / 2
        val innerRadius = radius * 0.4f
        val centerX = size.width / 2
        val centerY = size.height / 2

        ASPECT_SECTORS.forEach { sector ->
            val startAngle = sector.centerAngle - 22.5f
            val isSelected = selectedAspects.contains(sector.id)
            
            // Draw sector
            drawArc(
                color = if (isSelected) activeColor else inactiveColor,
                startAngle = startAngle,
                sweepAngle = 45f,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )

            // Draw sector outline
            drawArc(
                color = strokeColor,
                startAngle = startAngle,
                sweepAngle = 45f,
                useCenter = true,
                style = Stroke(width = 1.dp.toPx()),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(centerX - radius, centerY - radius)
            )

            // Draw Label
            val labelRadius = radius * 0.7f
            val labelAngleRad = Math.toRadians(sector.centerAngle.toDouble())
            val labelX = centerX + labelRadius * cos(labelAngleRad).toFloat()
            val labelY = centerY + labelRadius * sin(labelAngleRad).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.GRAY
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = (radius / 5).coerceAtLeast(10.dp.toPx())
                    isFakeBoldText = true
                }
                drawText(
                    sector.label,
                    labelX,
                    labelY + (paint.textSize / 3), // vertical centering adjust
                    paint
                )
            }
        }
        
        // Draw inner circle to make it look more like a rose/donut
        drawCircle(
            color = innerCircleColor,
            radius = innerRadius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = strokeColor,
            radius = innerRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
