package com.example.stratify.ui.full_analysis

import android.graphics.Color as AndroidColor // Alias to avoid conflict with Compose Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

// --- Colors ---
private val maroonPrimary = Color(0xFF800000)
private val textYellow = Color(0xFFFFEB3B)
private val backgroundLight = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(onBackPressed: () -> Unit) {
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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textYellow
                        )
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
            AppSummaryCard()
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
fun AppSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.Store,
                contentDescription = "App Logo",
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Shopee", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("4.6/5 Bintang", color = Color.Gray, fontSize = 12.sp)
            }
            // Placeholder for Donut Chart
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 40.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Chart Area", fontSize = 10.sp, color = Color.DarkGray)
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
                .height(250.dp), // Increased height slightly for better visibility
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            // Using AndroidView to wrap the MPAndroidChart XML-based view
            AndroidView(
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = true
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        axisRight.isEnabled = false

                        // Setup Dummy Data so the chart isn't empty
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
                        this.invalidate() // Refresh chart
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
            color = Color.Black,
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
                // Using trimMargin for cleaner code formatting
                keywords = """
                    • Good promo
                    • Free shipping
                    • Nice app
                """.trimIndent(),
                color = Color(0xFF2E7D32),
                backgroundColor = Color(0xFFE8F5E9),
                modifier = Modifier.weight(1f)
            )
            KeywordCard(
                title = "Negatif",
                keywords = """
                    • Bug
                    • Lambat
                    • Sulit digunakan
                """.trimIndent(),
                color = Color(0xFFD32F2F),
                backgroundColor = Color(0xFFFFEBEE),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KeywordCard(title: String, keywords: String, color: Color, backgroundColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = color
            )
            HorizontalDivider(
                modifier = Modifier
                    .width(30.dp)
                    .padding(vertical = 6.dp),
                thickness = 2.dp,
                color = color
            )
            Text(
                text = keywords,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ReviewFilterButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Created a helper wrapper or just repeat usage.
        // Note: 10.sp is very small, bumped to 11.sp
        FilterButton("Latest", Modifier.weight(1f))
        FilterButton("Positive", Modifier.weight(1f))
        FilterButton("Negative", Modifier.weight(1f))
    }
}

@Composable
fun FilterButton(text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp) // Reduce height slightly
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
            icon = Icons.Default.Person
        )
        Spacer(modifier = Modifier.height(8.dp))
        ReviewCard(
            author = "Siti N.",
            date = "10 Jan 2024",
            reviewText = "The app is interesting, lots of great features!",
            rating = 1,
            icon = Icons.Default.Person
        )
    }
}

@Composable
fun ReviewCard(author: String, date: String, reviewText: String, rating: Int, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
            Image(
                imageVector = icon,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray) // Add background to avatar placeholder
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("$author - $date", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(reviewText, fontSize = 13.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        // Logic to color stars based on rating
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