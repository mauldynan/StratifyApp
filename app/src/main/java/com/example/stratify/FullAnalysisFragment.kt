package com.example.stratify

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import com.example.stratify.ml.SentimentAnalyzer
import com.example.stratify.network.RetrofitClient
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

// PENTING: Karena SentimentAnalyzer & RetrofitClient ada di folder yang sama,
// kita TIDAK PERLU import manual.
// Jika masih merah, tekan Alt+Enter pada kata SentimentAnalyzer di bawah.

// --- Warna Desain ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)

// Model Data untuk UI
data class AnalyzedReview(
    val username: String,
    val content: String,
    val date: String,
    val sentiment: String // "Positif" atau "Negatif"
)

// =====================================================================
// 1. BAGIAN LOGIC (Stateful)
// =====================================================================
@Composable
fun FullAnalysisFragment(
    targetApp: String,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current

    // State
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var analyzedReviews by remember { mutableStateOf<List<AnalyzedReview>>(emptyList()) }
    var positiveCount by remember { mutableIntStateOf(0) }
    var negativeCount by remember { mutableIntStateOf(0) }

    // Logic: Download & Analisis ML
    LaunchedEffect(targetApp) {
        withContext(Dispatchers.IO) {
            try {
                // Inisialisasi SentimentAnalyzer
                // (Pastikan file SentimentAnalyzer.kt kamu punya class SentimentAnalyzer)
                val analyzer = SentimentAnalyzer(context)

                // Ambil data dari Python via RetrofitClient
                // (Pastikan file RetrofitClient.kt kamu punya object RetrofitClient)
                val rawReviews = RetrofitClient.instance.getReviews(targetApp)

                val results = rawReviews.map { review ->
                    // FUNGSI PREDICT DIPANGGIL DI SINI
                    val sentimentResult = analyzer.predict(review.content)

                    AnalyzedReview(
                        username = review.username,
                        content = review.content,
                        date = review.date,
                        sentiment = sentimentResult
                    )
                }

                val pos = results.count { it.sentiment == "Positif" }
                val neg = results.count { it.sentiment == "Negatif" }

                withContext(Dispatchers.Main) {
                    analyzedReviews = results
                    positiveCount = pos
                    negativeCount = neg
                    isLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isLoading = false
                    errorMessage = "Gagal terhubung ke Server.\nCek server Python atau Internet."
                }
            }
        }
    }

    // Panggil UI
    FullAnalysisContent(
        targetApp = targetApp,
        isLoading = isLoading,
        errorMessage = errorMessage,
        analyzedReviews = analyzedReviews,
        positiveCount = positiveCount,
        negativeCount = negativeCount,
        onBackPressed = onBackPressed
    )
}

// =====================================================================
// 2. BAGIAN TAMPILAN (Stateless)
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisContent(
    targetApp: String,
    isLoading: Boolean,
    errorMessage: String?,
    analyzedReviews: List<AnalyzedReview>,
    positiveCount: Int,
    negativeCount: Int,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ANALYSIS: ${targetApp.uppercase()}",
                        fontWeight = FontWeight.Black,
                        color = goldAccent,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = goldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = maroonPrimary)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(lightBg).padding(paddingValues)) {
            if (isLoading) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = maroonPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sedang mengambil data & analisis AI...", color = Color.Gray, fontSize = 12.sp)
                }
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                ) {
                    SummarySection(
                        appName = targetApp.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                        posCount = positiveCount,
                        negCount = negativeCount
                    )

                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("SENTIMENT TREND", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary.copy(0.6f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    LineChart(ctx).apply {
                                        description.isEnabled = false
                                        setDrawGridBackground(false)
                                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                                        axisRight.isEnabled = false
                                        setPinchZoom(true)
                                        legend.isEnabled = true
                                    }
                                },
                                update = { chart ->
                                    updateLineChartData(chart, positiveCount, negativeCount)
                                },
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("LATEST REVIEWS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary.copy(0.6f))
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp), tint = maroonPrimary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        analyzedReviews.forEach { review ->
                            ReviewItem(
                                name = review.username,
                                date = review.date,
                                comment = review.content,
                                sentiment = review.sentiment
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}

// --- Helper UI Components ---

@Composable
fun SummarySection(appName: String, posCount: Int, negCount: Int) {
    val total = posCount + negCount

    // Logic for dynamic logo
    val logoRes = when (appName.lowercase(Locale.ROOT)) {
        "shopee" -> R.drawable.shopee_logo
        "tokopedia" -> R.drawable.tokopedia_logo
        "tiktok" -> R.drawable.tiktok_logo
        "lazada" -> R.drawable.lazada_logo
        "bukalapak" -> R.drawable.bukalapak_logo
        "zalora" -> R.drawable.zalora_logo
        "amazon" -> R.drawable.amazon_logo
        else -> R.drawable.logo_stratify3 // Default logo if not found
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(maroonPrimary, lightBg)), alpha = 0.05f)
            .padding(24.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(appName, fontWeight = FontWeight.Black, fontSize = 20.sp, color = maroonPrimary)
                    Text("$total Reviews Analyzed", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text("$posCount Positif", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        Text(" • ", fontSize = 11.sp)
                        Text("$negCount Negatif", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(name: String, date: String, comment: String, sentiment: String) {
    val badgeColor = if (sentiment == "Positif") Color(0xFF10B981) else Color(0xFFEF4444)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(maroonPrimary.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), fontWeight = FontWeight.Black, color = maroonPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = maroonPrimary)
                    Surface(color = badgeColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(sentiment.uppercase(), color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(date, fontSize = 10.sp, color = Color.LightGray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(comment, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
            }
        }
    }
}

fun updateLineChartData(chart: LineChart, pos: Int, neg: Int) {
    val entriesPos = listOf(Entry(1f, pos * 0.2f), Entry(2f, pos * 0.5f), Entry(3f, pos.toFloat()))
    val entriesNeg = listOf(Entry(1f, neg * 0.2f), Entry(2f, neg * 0.6f), Entry(3f, neg.toFloat()))

    val set1 = LineDataSet(entriesPos, "Positif").apply {
        color = "#10B981".toColorInt()
        lineWidth = 2f
        setDrawCircles(false)
    }
    val set2 = LineDataSet(entriesNeg, "Negatif").apply {
        color = "#EF4444".toColorInt()
        lineWidth = 2f
        setDrawCircles(false)
    }
    chart.data = LineData(set1, set2)
    chart.invalidate()
}

// =====================================================================
// 3. PREVIEW SECTION
// =====================================================================
@Preview(showBackground = true, name = "Full Analysis - Loading")
@Composable
fun PreviewLoading() {
    FullAnalysisContent(
        targetApp = "Shopee",
        isLoading = true,
        errorMessage = null,
        analyzedReviews = emptyList(),
        positiveCount = 0,
        negativeCount = 0,
        onBackPressed = {}
    )
}

@Preview(showBackground = true, name = "Full Analysis - Success", heightDp = 800)
@Composable
fun PreviewSuccess() {
    val dummyReviews = listOf(
        AnalyzedReview("Budi", "Aplikasi sangat bagus!", "2023-10-01", "Positif"),
        AnalyzedReview("Siti", "Pengiriman agak lama ya", "2023-10-02", "Negatif")
    )

    FullAnalysisContent(
        targetApp = "Shopee",
        isLoading = false,
        errorMessage = null,
        analyzedReviews = dummyReviews,
        positiveCount = 1,
        negativeCount = 1,
        onBackPressed = {}
    )
}
