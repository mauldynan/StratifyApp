package com.example.stratify.ui.full_analysis

import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.stratify.DonutChart
import com.example.stratify.R
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.view.profile.SharedViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

// --- Design Tokens ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)
private val emeraldGreen = Color(0xFF10B981)
private val roseRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(
    navController: NavController,
    viewModel: SharedViewModel = viewModel(),
    onBackPressed: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Latest") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FULL ANALYSIS",
                        fontWeight = FontWeight.Black,
                        color = goldAccent,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(lightBg)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER: App Summary Card ---
            SummarySection()

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                // --- SECTION 2: Chart ---
                Text(
                    "SENTIMENT OVER TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = maroonPrimary.copy(0.6f),
                    modifier = Modifier.padding(top = 0.dp, bottom = 8.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth().height(240.dp)
                ) {
                    AndroidView(
                        factory = { context ->
                            LineChart(context).apply {
                                description.isEnabled = false
                                setDrawGridBackground(false)
                                axisRight.isEnabled = false
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.setDrawGridLines(false)
                                axisLeft.setDrawGridLines(true)
                                axisLeft.gridColor = AndroidColor.LTGRAY
                                setTouchEnabled(true)
                                setPinchZoom(true)
                                legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.TOP
                                legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.RIGHT
                            }
                        },
                        update = { lineChart -> updateLineChartData(lineChart) },
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    )
                }

                // --- SECTION 3: Keywords ---
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "DOMINANT KEYWORDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = maroonPrimary.copy(0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    KeywordCard(
                        title = "Positive",
                        icon = Icons.Default.TrendingUp,
                        color = emeraldGreen,
                        items = listOf("Free shipping", "Good promo", "Nice app"),
                        modifier = Modifier.weight(1f)
                    )
                    KeywordCard(
                        title = "Negative",
                        icon = Icons.Default.TrendingDown,
                        color = roseRed,
                        items = listOf("Slow app", "Bugs", "Customer support"),
                        modifier = Modifier.weight(1f)
                    )
                }

                // --- SECTION 4: Review List ---
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("USER REVIEWS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary.copy(0.6f))
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp), tint = maroonPrimary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Latest", "Positive", "Negative").forEach { filter ->
                        FilterChip(
                            label = filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val appData = viewModel.ecommerceApps.find { it.name == "Shopee" }
                appData?.latestComments?.forEach { (name, comment) ->
                    ReviewItem(
                        name = name,
                        date = "Recently",
                        comment = comment,
                        rating = 5
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SummarySection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(maroonPrimary, lightBg)),
                alpha = 0.05f
            )
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp)
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
                    painter = painterResource(id = R.drawable.shopee_logo),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Shopee", fontWeight = FontWeight.Black, fontSize = 20.sp, color = maroonPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = goldAccent, modifier = Modifier.size(14.dp))
                        Text(" 4.8", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                        Text(" • 1.2M Reviews", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                DonutChart(
                    modifier = Modifier.size(70.dp),
                    positivePercent = 70f,
                    negativePercent = 30f,
                    totalReviews = 1200
                )
            }
        }
    }
}

@Composable
fun KeywordCard(title: String, icon: ImageVector, color: Color, items: List<String>, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Black, fontSize = 12.sp, color = color)
            }
            Spacer(Modifier.height(12.dp))
            items.forEach { item ->
                Text("• $item", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(CapsuleShape)
            .clickable { onClick() },
        color = if (isSelected) maroonPrimary else Color.White,
        shape = CircleShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) maroonPrimary else Color.LightGray.copy(0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

private val CapsuleShape = RoundedCornerShape(50)

@Composable
fun ReviewItem(name: String, date: String, comment: String, rating: Int) {
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
                Text(name.take(1), fontWeight = FontWeight.Black, color = maroonPrimary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(name, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = maroonPrimary)
                    Text(date, fontSize = 10.sp, color = Color.LightGray)
                }

                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    repeat(5) { index ->
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = if (index < rating) goldAccent else Color.LightGray.copy(0.3f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Text(comment, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 18.sp)
            }
        }
    }
}

// Logic Helper tetap sama dengan perbaikan visual grid
fun updateLineChartData(lineChart: LineChart) {
    val positiveEntries = listOf(Entry(1f, 70f), Entry(2f, 75f), Entry(3f, 68f), Entry(4f, 80f), Entry(5f, 85f))
    val negativeEntries = listOf(Entry(1f, 30f), Entry(2f, 25f), Entry(3f, 32f), Entry(4f, 20f), Entry(5f, 15f))

    val positiveSet = LineDataSet(positiveEntries, "Positive").apply {
        color = AndroidColor.parseColor("#10B981")
        setCircleColor(AndroidColor.parseColor("#10B981"))
        lineWidth = 3f
        circleRadius = 4f
        setDrawCircleHole(false)
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(false)
    }

    val negativeSet = LineDataSet(negativeEntries, "Negative").apply {
        color = AndroidColor.parseColor("#EF4444")
        setCircleColor(AndroidColor.parseColor("#EF4444"))
        lineWidth = 3f
        circleRadius = 4f
        setDrawCircleHole(false)
        mode = LineDataSet.Mode.CUBIC_BEZIER
        setDrawValues(false)
    }

    lineChart.data = LineData(positiveSet, negativeSet)
    lineChart.animateX(1000)
    lineChart.invalidate()
}

@Preview(showBackground = true)
@Composable
fun FullAnalysisScreenPreview() {
    StratifyTheme {
        FullAnalysisScreen(navController = rememberNavController(), onBackPressed = {})
    }
}