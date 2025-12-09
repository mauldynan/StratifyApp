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
 * Komponen Donut Chart murni menggunakan Jetpack Compose.
 */
@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    positivePercent: Float,
    negativePercent: Float,
    totalReviews: Int
) {
    // Animasi pergerakan chart saat muncul
    val animatedSweep = remember { Animatable(0f) }

    LaunchedEffect(positivePercent, negativePercent) {
        animatedSweep.snapTo(0f)
        animatedSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    // Definisi Warna
    val positiveColor = Color(0xFF4CAF50)
    val negativeColor = Color(0xFFF44336)
    val backgroundColor = Color(0xFFE0E0E0)
    val strokeWidth = 12.dp // Sedikit diperkecil biar rapi

    Row(
        modifier = modifier, // Menggunakan modifier dari parent (agar posisi fleksibel)
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // --- BAGIAN LINGKARAN CHART ---
        Box(
            modifier = Modifier.size(90.dp), // Ukuran fixed disesuaikan dengan layout Card
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = strokeWidth.toPx()
                val diameter = size.minDimension - strokePx
                val topLeft = Offset(strokePx / 2, strokePx / 2)

                // 1. Gambar Background Abu-abu (Lingkaran Penuh)
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
                // Menghitung sudut berdasarkan persentase
                val positiveAngle = 360f * (positivePercent / totalPercent)
                val negativeAngle = 360f * (negativePercent / totalPercent)

                // 2. Gambar Garis Hijau (Positif)
                drawArc(
                    color = positiveColor,
                    startAngle = -90f, // Mulai dari jam 12
                    sweepAngle = positiveAngle * animatedSweep.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )

                // 3. Gambar Garis Merah (Negatif)
                drawArc(
                    color = negativeColor,
                    startAngle = -90f + positiveAngle, // Lanjut dari posisi hijau
                    sweepAngle = negativeAngle * animatedSweep.value,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )
            }

            // Teks Angka di Tengah Lingkaran
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = totalReviews.toString(),
                    fontSize = 14.sp, // Font size disesuaikan agar muat
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Reviews",
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- BAGIAN LEGENDA (Teks Samping) ---
        Column {
            Text(
                text = "${positivePercent.toInt()}% Pos",
                color = positiveColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${negativePercent.toInt()}% Neg",
                color = negativeColor,
                fontSize = 12.sp,
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