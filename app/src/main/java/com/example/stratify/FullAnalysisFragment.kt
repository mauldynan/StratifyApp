package com.example.stratify

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class FullAnalysisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Kita menggunakan ComposeView sebagai root view, menggantikan XML inflater
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    // Panggil fungsi Composable utama di sini
                    FullAnalysisScreen(
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Analysis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // Column Utama (Pengganti ScrollView di XML)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // --- BAGIAN 1: SHOPEE CARD & DONUT CHART ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Shopee
                    // Pastikan kamu punya gambar 'shopee_logo' di res/drawable
                    Image(
                        painter = painterResource(id = R.drawable.shopee_logo),
                        contentDescription = "Shopee Logo",
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Teks Nama & Rating
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shopee", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("4.6/5 Bintang", fontSize = 12.sp, color = Color.Gray)
                    }

                    // Donut Chart (Memanggil file DonutChart.kt)
                    Box(modifier = Modifier.size(width = 160.dp, height = 90.dp)) {
                        DonutChart(
                            positivePercent = 70f,
                            negativePercent = 30f,
                            totalReviews = 1250,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BAGIAN 2: SENTIMENT CHART (MPAndroidChart) ---
            Text("Sentiment Analysis Chart", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                // Menggunakan AndroidView agar bisa pakai library lama (MPAndroidChart) di Compose
                AndroidView(
                    factory = { context ->
                        LineChart(context).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            axisRight.isEnabled = false
                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.setDrawGridLines(false)
                            axisLeft.textColor = AndroidColor.DKGRAY
                            xAxis.textColor = AndroidColor.DKGRAY
                            legend.isEnabled = true
                        }
                    },
                    update = { lineChart ->
                        updateLineChartData(lineChart)
                    },
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BAGIAN 3: DOMINANT KEYWORDS ---
            Text(
                text = "Dominant Keywords",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF800000), // Warna Maroon
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Kartu Keyword Positif
                KeywordCard(
                    title = "Positif",
                    color = Color(0xFF4CAF50), // Hijau
                    bgColor = Color(0xFFE8F9EE),
                    items = listOf("Good promo", "Free shipping", "Nice app"),
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )

                // Kartu Keyword Negatif
                KeywordCard(
                    title = "Negatif",
                    color = Color(0xFFF44336), // Merah
                    bgColor = Color(0xFFFFF1F1),
                    items = listOf("Bug", "Lambat", "Sulit digunakan"),
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BAGIAN 4: FILTER BUTTONS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterButton("Latest", Modifier.weight(1f))
                FilterButton("Positive", Modifier.weight(1f))
                FilterButton("Negative", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BAGIAN 5: LIST REVIEWS ---
            Text("Reviews", fontSize = 16.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            ReviewItem(
                name = "Budi S. - 12 Jan 2024",
                comment = "The app makes work very easy",
                rating = "⭐⭐⭐⭐⭐ 5/5"
            )

            Spacer(modifier = Modifier.height(8.dp))

            ReviewItem(
                name = "Siti N. - 10 Jan 2024",
                comment = "The app is interesting, lots of great features!",
                rating = "⭐ 1/5"
            )

            // Tambahan padding bawah agar tidak kepotong
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// ==========================================
// SUB-COMPOSABLES (Komponen Kecil)
// ==========================================

@Composable
fun KeywordCard(title: String, color: Color, bgColor: Color, items: List<String>, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier
                .width(30.dp)
                .height(2.dp)
                .background(color))
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Text("• $item", fontSize = 13.sp, lineHeight = 20.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun FilterButton(text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = {},
        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.height(35.dp)
    ) {
        Text(text, fontSize = 10.sp, color = Color.White)
    }
}

@Composable
fun ReviewItem(name: String, comment: String, rating: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            // Pastikan kamu punya gambar 'ic_acc' di res/drawable
            Image(
                painter = painterResource(id = R.drawable.ic_acc),
                contentDescription = "User Icon",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(comment, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                Text(rating, fontSize = 12.sp, color = Color(0xFF800000))
            }
        }
    }
}

// ==========================================
// LOGIC HELPER UNTUK CHART (MPAndroidChart)
// ==========================================

fun updateLineChartData(lineChart: LineChart) {
    val positiveEntries = listOf(Entry(1f, 1f), Entry(2f, 2f), Entry(3f, 1.5f), Entry(4f, 2.5f), Entry(5f, 3f))
    val negativeEntries = listOf(Entry(1f, 0.5f), Entry(2f, 1f), Entry(3f, 0.8f), Entry(4f, 1.5f), Entry(5f, 2f))

    val positiveSet = LineDataSet(positiveEntries, "Positive").apply {
        color = AndroidColor.parseColor("#4CAF50")
        lineWidth = 2f
        setCircleColor(AndroidColor.parseColor("#4CAF50"))
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(false)
    }

    val negativeSet = LineDataSet(negativeEntries, "Negative").apply {
        color = AndroidColor.parseColor("#F44336")
        lineWidth = 2f
        setCircleColor(AndroidColor.parseColor("#F44336"))
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(false)
    }

    lineChart.data = LineData(positiveSet, negativeSet)
    lineChart.invalidate()
}