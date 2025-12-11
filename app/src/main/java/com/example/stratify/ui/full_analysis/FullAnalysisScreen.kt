package com.example.stratify.ui.full_analysis

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.stratify.DonutChart
import com.example.stratify.R
import com.example.stratify.ml.SentimentAnalyzer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

// --- Data class to hold review information ---
data class Review(
    val author: String,
    val date: String,
    val reviewText: String,
    val rating: Int,
    val initials: String
)

// --- Colors --
private val maroonPrimary = Color(0xFF760000)
private val textYellow = Color(0xFFF6C761)
private val backgroundLight = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(navController: NavController, onBackPressed: () -> Unit) {

    // 1. Get an instance of our SentimentAnalyzer
    val sentimentAnalyzer = SentimentAnalyzer.rememberSentimentAnalyzer()

    // 2. Define the list of reviews to analyze (in a real app, this would come from a ViewModel)
    val reviews = remember {
        listOf(
            Review("Budi S.", "12 Jan 2024", "The app makes work very easy", 5, "B"),
            Review("Siti N.", "10 Jan 2024", "The app is interesting, lots of great features!", 1, "S")
        )
    }

    // 3. Create state holders for our analysis results
    var positivePercentage by remember { mutableStateOf(0f) }
    var negativePercentage by remember { mutableStateOf(0f) }
    var chartEntries by remember { mutableStateOf<List<Entry>>(emptyList()) }

    // 4. Run the analysis when the screen launches
    LaunchedEffect(reviews, sentimentAnalyzer) {
        if (reviews.isNotEmpty()) {
            val results = reviews.map { review ->
                // Get the top prediction from the model ("Positive" or "Negative")
                sentimentAnalyzer.classify(review.reviewText).maxByOrNull { it.second }?.first
            }

            val positiveCount = results.count { it == "Positive" }
            val negativeCount = results.count { it == "Negative" }
            val total = reviews.size

            positivePercentage = (positiveCount.toFloat() / total) * 100
            negativePercentage = (negativeCount.toFloat() / total) * 100

            // Create entries for the line chart (1f for Positive, 0f for Negative)
            chartEntries = results.mapIndexed { index, sentiment ->
                val sentimentValue = if (sentiment == "Positive") 1f else 0f
                Entry(index.toFloat(), sentimentValue)
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Full Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textYellow
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textYellow)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = maroonPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(backgroundLight)
                .padding(16.dp)
        ) {
            // 5. Pass the real data to our composables
            AppSummaryCard(
                navController = navController,
                positivePercent = positivePercentage,
                negativePercent = negativePercentage,
                totalReviews = reviews.size
            )
            Spacer(modifier = Modifier.height(20.dp))
            SentimentAnalysisChart(entries = chartEntries)
            Spacer(modifier = Modifier.height(16.dp))
            DominantKeywordsSection()
            Spacer(modifier = Modifier.height(16.dp))
            ReviewFilterButtons()
            Spacer(modifier = Modifier.height(12.dp))
            ReviewsSection(reviews = reviews) // Pass the original reviews list
        }
    }
}

@Composable
fun AppSummaryCard(
    navController: NavController,
    positivePercent: Float,
    negativePercent: Float,
    totalReviews: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.shopee_logo),
                contentDescription = "Shopee Logo",
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Shopee", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("4.6/5 Bintang", color = Color.Gray, fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clickable { navController.navigate("full_analysis_detail") },
                contentAlignment = Alignment.Center
            ) {
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
    Column {
        Text("Sentiment Analysis Chart", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    if (entries.isNotEmpty()) {
                        val dataSet = LineDataSet(entries, "Sentiment (1=Positive, 0=Negative)").apply {
                            color = AndroidColor.RED
                            valueTextColor = AndroidColor.BLACK
                            lineWidth = 2f
                            setDrawCircles(true)
                        }
                        chart.data = LineData(dataSet)
                        chart.invalidate()
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun DominantKeywordsSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Dominant Keywords",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = maroonPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeywordCard(
                title = "Positif",
                keywords = listOf("Good promo", "Free shipping", "Nice app"),
                color = Color(0xFF2E7D32),
                backgroundColor = Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f)
            )
            KeywordCard(
                title = "Negatif",
                keywords = listOf("Bug", "Lambat", "Sulit digunakan"),
                color = Color(0xFFD32F2F),
                backgroundColor = Color(0xFFFFEBEE),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KeywordCard(title: String, keywords: List<String>, color: Color, backgroundColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = color)
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier
                .width(30.dp)
                .height(2.dp)
                .background(color))
            Spacer(modifier = Modifier.height(8.dp))
            keywords.forEach { keyword ->
                Text("• $keyword", fontSize = 13.sp, lineHeight = 18.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun ReviewFilterButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton("Latest", Modifier.weight(1f))
        FilterButton("Positive", Modifier.weight(1f))
        FilterButton("Negative", Modifier.weight(1f))
    }
}

@Composable
fun FilterButton(text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { },
        modifier = modifier.height(35.dp),
        colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 11.sp, color = textYellow)
    }
}

@Composable
fun ReviewsSection(reviews: List<Review>) {
    Column {
        Text("Reviews", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        reviews.forEach { review ->
            ReviewCard(
                author = review.author,
                date = review.date,
                reviewText = review.reviewText,
                rating = review.rating,
                initials = review.initials
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ReviewCard(author: String, date: String, reviewText: String, rating: Int, initials: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // ... implementation of ReviewCard
    }
}
