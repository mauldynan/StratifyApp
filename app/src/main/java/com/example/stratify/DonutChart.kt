package com.example.stratify

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Jetpack Compose composable that displays a donut chart with animated segments
 * and labels, similar to the original DonutChartView.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param positivePercent The percentage of the chart representing the "positive" segment.
 * @param negativePercent The percentage of the chart representing the "negative" segment.
 * @param totalReviews The total number to display in the center of the chart.
 */
@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    positivePercent: Float,
    negativePercent: Float,
    totalReviews: Int
) {
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(positivePercent, negativePercent) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    val positiveColor = Color(0xFF4CAF50)
    val negativeColor = Color(0xFFF44336)
    val backgroundColor = Color(0xFFE0E0E0)
    val strokeWidth = 15.dp

    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset(strokePx / 2, strokePx / 2)

                drawArc(
                    color = backgroundColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx),
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )


                val totalPercent = (positivePercent + negativePercent).coerceAtMost(100f)
                val positiveAngle = 360f * (positivePercent / totalPercent)
                val negativeAngle = 360f * (negativePercent / totalPercent)

                drawArc(
                    color = positiveColor,
                    startAngle = -90f, // Start from the top
                    sweepAngle = positiveAngle * animatedSweep.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )

                drawArc(
                    color = negativeColor,
                    startAngle = -90f + positiveAngle,
                    sweepAngle = negativeAngle * animatedSweep.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalReviews.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Total Review",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column {
            Text(
                text = "${positivePercent.toInt()}% Positive",
                color = positiveColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${negativePercent.toInt()}% Negative",
                color = negativeColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DonutChartPreview() {
    DonutChart(
        positivePercent = 70f,
        negativePercent = 30f,
        totalReviews = 1200
    )
}
