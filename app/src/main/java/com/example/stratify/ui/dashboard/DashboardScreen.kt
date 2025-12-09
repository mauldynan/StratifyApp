package com.example.stratify.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stratify.DonutChart
import com.example.stratify.R
import com.example.stratify.Screen

// --- Colors ---
private val maroonPrimary = Color(0xFF800000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_stratify2),
                        contentDescription = "Stratify Logo",
                        modifier = Modifier.height(30.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = maroonPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                placeholder = { Text("Search", color = Color(0xFFB0B0B0)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Gray,
                    unfocusedBorderColor = Color.LightGray,
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Monitored Applications Section
            SectionHeader("Monitored Applications")
            Spacer(modifier = Modifier.height(8.dp))
            MonitoredAppCard(navController = navController)

            Spacer(modifier = Modifier.height(24.dp))

            // Recently Viewed Apps Section
            SectionHeader("Recently Viewed Apps")
            Spacer(modifier = Modifier.height(8.dp))
            RecentlyViewedApps()

            Spacer(modifier = Modifier.height(24.dp))

            // Recommendation Apps Section
            SectionHeader("Recommendation Apps")
            Spacer(modifier = Modifier.height(8.dp))
            RecommendationAppItem(
                icon = Icons.Default.Star,
                appName = "Blibli",
                rating = "4.8 / 5 Bintang"
            )
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
fun MonitoredAppCard(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = Icons.Default.ShoppingCart, // Placeholder for Shopee logo
                contentDescription = "Shopee Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(start = 8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = "Shopee",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "4.6 / 5 Bintang",
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )
            }

            Column(
                modifier = Modifier.padding(end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DonutChart(
                    modifier = Modifier.size(width = 140.dp, height = 80.dp),
                    positivePercent = 70f,
                    negativePercent = 30f,
                    totalReviews = 1200
                )
                Button(
                    onClick = { navController.navigate(Screen.FullAnalysis.route) },
                    modifier = Modifier
                        .height(32.dp)
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF800000))
                ) {
                    Text("View Full Analysis", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RecentlyViewedApps() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Placeholders for recently viewed app logos
        AppIcon(icon = Icons.Default.Favorite)
        AppIcon(icon = Icons.Default.ShoppingCart)
        AppIcon(icon = Icons.Default.Star)
        AppIcon(icon = Icons.Default.Place)
    }
}

@Composable
fun AppIcon(icon: ImageVector) {
    Image(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .padding(8.dp)
    )
}

@Composable
fun RecommendationAppItem(icon: ImageVector, appName: String, rating: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon = icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = appName, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(text = rating, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Go to details",
            tint = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // Preview doesn't have a NavController, so we can't show the full screen.
    // We can either pass a fake NavController or show a simplified version of the screen.
}
