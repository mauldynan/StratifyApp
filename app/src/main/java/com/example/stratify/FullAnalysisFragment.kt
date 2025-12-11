package com.example.stratify

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.stratify.data.CsvDataParser
import com.example.stratify.ml.SentimentAnalyzer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class FullAnalysisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FullAnalysisScreen(
                        onBackClick = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}

// Data class to hold the results of the analysis
private data class SentimentAnalysisResult(
    val positivePercent: Float = 0f,
    val negativePercent: Float = 0f,
    val totalReviews: Int = 0,
    val chartEntries: List<Entry> = emptyList(),
    val classifiedReviews: List<Pair<String, String>> = emptyList() // Pair<ReviewText, SentimentLabel>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sentimentAnalyzer = SentimentAnalyzer.rememberSentimentAnalyzer()

    // State to hold the results of our analysis
    var analysisResult by remember { mutableStateOf(SentimentAnalysisResult()) }

    // This effect runs once to load data and perform analysis
    LaunchedEffect(Unit) {
        val reviews = CsvDataParser.getReviewsFromCsv(context)
        val totalReviews = reviews.size
        if (totalReviews == 0) return@LaunchedEffect

        var positiveCount = 0
        val chartEntries = mutableListOf<Entry>()
        val classifiedReviews = mutableListOf<Pair<String, String>>()

        reviews.forEachIndexed { index, reviewText ->
            val classification = sentimentAnalyzer.classify(reviewText)
            // Get the label with the highest score ("Positive" or "Negative")
            val topResult = classification.maxByOrNull { it.second }
            val sentiment = topResult?.first ?: "Unknown"

            if (sentiment.equals("Positive", ignoreCase = true)) {
                positiveCount++
                // Use 1f for Positive for a clearer chart
                chartEntries.add(Entry(index.toFloat(), 1f))
            } else if (sentiment.equals("Negative", ignoreCase = true)) {
                // Use 0f for Negative
                chartEntries.add(Entry(index.toFloat(), 0f))
            }
            classifiedReviews.add(reviewText to sentiment)
        }

        val positivePercent = (positiveCount.toFloat() / totalReviews) * 100
        val negativePercent = 100 - positivePercent

        // Update the state, which will trigger a recomposition to update the UI
        analysisResult = SentimentAnalysisResult(
            positivePercent = positivePercent,
            negativePercent = negativePercent,
            totalReviews = totalReviews,
            chartEntries = chartEntries,
            classifiedReviews = classifiedReviews
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Analysis", fontWeight = FontWeight.Bold, color = colorResource(id = R.color.app_yellow)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- BAGIAN 1: SHOPEE CARD & DONUT CHART (DATA-DRIVEN) ---
            item {
                AppSummaryCard(
                    positivePercent = analysisResult.positivePercent,
                    negativePercent = analysisResult.negativePercent,
                    totalReviews = analysisResult.totalReviews
                )
            }

            // --- BAGIAN 2: SENTIMENT CHART (DATA-DRIVEN) ---
            item {
                Text("Sentiment Analysis Chart", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                SentimentAnalysisChart(entries = analysisResult.chartEntries)
            }

            // --- BAGIAN 3: DOMINANT KEYWORDS (STATIC) ---
            item {
                DominantKeywordsSection()
            }

            // --- BAGIAN 4: FILTER BUTTONS (STATIC) ---
            item {
                FilterButtonsSection()
            }

            // --- BAGIAN 5: LIST REVIEWS (DATA-DRIVEN) ---
            item {
                Text("Reviews", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            items(analysisResult.classifiedReviews) { (review, sentiment) ->
                ReviewItem(
                    comment = review,
                    sentiment = sentiment
                )
            }
        }
    }
}

// =========================================
// SUB-COMPOSABLES (Refactored for clarity)
// =========================================

@Composable
fun AppSummaryCard(positivePercent: Float, negativePercent: Float, totalReviews: Int) {
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
            Image(
                painter = painterResource(id = R.drawable.shopee_logo),
                contentDescription = "Shopee Logo",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Shopee", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text("4.6/5 Bintang", fontSize = 12.sp, color = Color.Gray)
            }
            Box(modifier = Modifier.size(width = 160.dp, height = 90.dp)) {
                DonutChart(
                    positivePercent = positivePercent,
                    negativePercent = negativePercent,
                    totalReviews = totalReviews,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun SentimentAnalysisChart(entries: List<Entry>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    description.isEnabled = false
                    setDrawGridBackground(false)
                    axisRight.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    axisLeft.setDrawLabels(false) // Hide Y-axis labels for simplicity
                    legend.isEnabled = false // Hide legend for a cleaner look with one line
                }
            },
            update = { lineChart ->
                updateLineChartWithData(lineChart, entries)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Composable
fun DominantKeywordsSection() {
    Text(
        text = "Dominant Keywords",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF800000),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        KeywordCard(
            title = "Positif",
            color = Color(0xFF4CAF50),
            bgColor = Color(0xFFE8F9EE),
            items = listOf("Good promo", "Free shipping", "Nice app"),
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
        )
        KeywordCard(
            title = "Negatif",
            color = Color(0xFFF44336),
            bgColor = Color(0xFFFFF1F1),
            items = listOf("Bug", "Lambat", "Sulit digunakan"),
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
        )
    }
}

@Composable
fun FilterButtonsSection() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterButton("Latest", Modifier.weight(1f))
        FilterButton("Positive", Modifier.weight(1f))
        FilterButton("Negative", Modifier.weight(1f))
    }
}


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
            Box(modifier = Modifier.width(30.dp).height(2.dp).background(color))
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
fun ReviewItem(comment: String, sentiment: String) {
    val sentimentColor = if (sentiment.equals("Positive", ignoreCase = true)) Color(0xFF4CAF50) else Color(0xFFF44336)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_acc),
                contentDescription = "User Icon",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(comment, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp), maxLines = 3)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                sentiment,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = sentimentColor,
                modifier = Modifier
                    .background(sentimentColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

fun updateLineChartWithData(lineChart: LineChart, entries: List<Entry>) {
    if (entries.isEmpty()) {
        lineChart.clear()
        lineChart.invalidate()
        return
    }

    val dataSet = LineDataSet(entries, "Sentiment").apply {
        color = AndroidColor.parseColor("#800000") // Maroon color for the line
        lineWidth = 2f
        setDrawCircles(false)
        setDrawValues(false)
    }

    val lineData = LineData(dataSet)
    lineChart.data = lineData
    lineChart.invalidate() // Refresh the chart
}
