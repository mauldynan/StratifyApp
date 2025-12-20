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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.ui.theme.StratifyTheme

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
    val strokeWidth = 10.dp // Lebih tipis agar proporsional

    Row(
        modifier = modifier.fillMaxSize(), // gunakan full width dari modifier agar alignment bekerja
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start // geser donut ke kiri
    ) {
        // --- BAGIAN LINGKARAN CHART ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = 4.dp) // Turunkan sedikit ke tengah
            ) {
                Text(
                    text = totalReviews.toString(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Total Review",
                    modifier = Modifier.offset(y = (-8).dp),
                    fontSize = 6.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))

        // --- BAGIAN LEGENDA (Teks Samping) ---
//        Column {
//            Text(
//                text = "${positivePercent.toInt()}% Positive",
//                color = positiveColor,
//                fontSize = 10.sp,
//                fontWeight = FontWeight.Bold
//            )
//            Spacer(modifier = Modifier.height(-2.dp))
//            Text(
//                text = "${negativePercent.toInt()}% Negative",
//                color = negativeColor,
//                fontSize = 10.sp,
//                fontWeight = FontWeight.Bold
//            )
//        }
    }
}


@Preview(showBackground = true)
@Composable
fun DonutChartPreview() {
    StratifyTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    modifier = Modifier.size(width = 200.dp, height = 100.dp),
                    positivePercent = 75f,
                    negativePercent = 25f,
                    totalReviews = 1560
                )
            }
        }
    }
}
