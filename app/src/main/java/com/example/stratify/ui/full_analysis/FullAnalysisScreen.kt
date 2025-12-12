package com.example.stratify.ui.full_analysis

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stratify.DonutChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.example.stratify.R



// --- Colors ---
private val maroonPrimary = Color(0xFF760000)
private val textYellow = Color(0xFFF6C761)
private val backgroundLight = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(navController: NavController, onBackPressed: () -> Unit) {
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
            AppSummaryCard(navController)
            Spacer(modifier = Modifier.height(20.dp))
            SentimentAnalysisChart()
            Spacer(modifier = Modifier.height(16.dp))
            DominantKeywordsSection()
            Spacer(modifier = Modifier.height(16.dp))
            ReviewFilterButtons()
            Spacer(modifier = Modifier.height(12.dp))
            ReviewsSection()
        }
    }
}

@Composable
fun AppSummaryCard(navController: NavController) {
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
            // Shopee Logo
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

            // DonutChart
            Box(
                modifier = Modifier
                    .size(140.dp) // ukuran besar sesuai keinginan
                    .alignBy { 70 } // pastikan rata vertikal dengan logo (70 = tinggi logo)
                    .clickable { navController.navigate("full_analysis_detail") },
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    positivePercent = 70f,
                    negativePercent = 30f,
                    totalReviews = 1200,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

    }
}


@Composable
fun SentimentAnalysisChart() {
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

                        val entries = listOf(
                            Entry(1f, 3f),
                            Entry(2f, 4f),
                            Entry(3f, 2f),
                            Entry(4f, 5f),
                            Entry(5f, 4.5f)
                        )
                        val dataSet = LineDataSet(entries, "Sentiment").apply {
                            color = AndroidColor.RED
                            valueTextColor = AndroidColor.BLACK
                            lineWidth = 2f
                            setDrawCircles(true)
                        }
                        this.data = LineData(dataSet)
                        this.invalidate()
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
fun ReviewsSection() {
    Column {
        Text("Reviews", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ReviewCard(
            author = "Budi S.",
            date = "12 Jan 2024",
            reviewText = "The app makes work very easy",
            rating = 5,
            initials = "B"
        )
        Spacer(modifier = Modifier.height(8.dp))
        ReviewCard(
            author = "Siti N.",
            date = "10 Jan 2024",
            reviewText = "The app is interesting, lots of great features!",
            rating = 1,
            initials = "S"
        )
    }
}

@Composable
fun ReviewCard(author: String, date: String, reviewText: String, rating: Int, initials: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("$author - $date", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(reviewText, fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        val starColor = if (index < rating) maroonPrimary else Color.LightGray
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = starColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$rating/5",
                        color = maroonPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
