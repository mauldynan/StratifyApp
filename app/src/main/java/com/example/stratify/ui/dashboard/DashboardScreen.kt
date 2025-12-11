package com.example.stratify.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.stratify.DonutChart
import com.example.stratify.R
import com.example.stratify.Screen
import kotlinx.coroutines.launch


// --- Colors ---
private val maroonPrimary = Color(0xFF800000)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController = navController, onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo_stratify3),
                            contentDescription = "Stratify Logo",
                            modifier = Modifier.height(72.dp)
                        )
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_acc),
                                contentDescription = "Account",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                            )
                        }
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
                // Title for the screen section
                var searchQuery by remember { mutableStateOf("") }
                Text(
                    text = "Analysis Apps",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = maroonPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),

                    placeholder = { Text("Search", color = Color(0xFFB0B0B0)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = maroonPrimary,
                        unfocusedBorderColor = Color.LightGray,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
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

                RecommendationAppItemCard(
                    painter = painterResource(id = R.drawable.zalora_logo),
                    appName = "Zalora",
                    rating = "4.8 / 5 Bintang",
                    sales = "2.3M Terjual",
                    onClick = { /* navigasi ke detail Zalora */ }
                )

                RecommendationAppItemCard(
                    painter = painterResource(id = R.drawable.tokopedia_logo),
                    appName = "Tokopedia",
                    rating = "4.6 / 5 Bintang",
                    sales = "3.5M Terjual",
                    onClick = { /* navigasi ke detail Tokopedia */ }
                )
            }
        }
    }
}

@Composable
fun DrawerContent(navController: NavController, onCloseDrawer: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(maroonPrimary)
                .padding(vertical = 32.dp, horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Menu", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        NavigationDrawerItem(
            label = { Text("Profile") },
            selected = false,
            onClick = {
                navController.navigate(Screen.ProfileOptions.route)
                onCloseDrawer()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = { /* TODO: Handle Logout */ onCloseDrawer() },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
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
                painter = painterResource(id = R.drawable.shopee_logo),
                contentDescription = "Shopee Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(start = 8.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 12.dp)
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

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.padding(end = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DonutChart(
                    modifier = Modifier.size(width = 140.dp, height = 70.dp),
                    positivePercent = 70f,
                    negativePercent = 30f,
                    totalReviews = 1200
                )
                Button(
                    onClick = { navController.navigate(Screen.FullAnalysis.route) },
                    modifier = Modifier
                        .height(32.dp)
                        .width(140.dp)
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
        AppIcon(painter = painterResource(id = R.drawable.lazada_logo))
        AppIcon(painter = painterResource(id = R.drawable.tokopedia_logo))
        AppIcon(painter = painterResource(id = R.drawable.amazon_logo))
        AppIcon(painter = painterResource(id = R.drawable.bukalapak_logo))
    }
}

@Composable
fun AppIcon(icon: ImageVector? = null, painter: Painter? = null, modifier: Modifier = Modifier) {
    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
                .padding(8.dp)
        )
    } else if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
                .padding(8.dp)
        )
    }
}

@Composable
fun RecommendationAppItemCard(
    painter: Painter,
    appName: String,
    rating: String,
    sales: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(painter = painter)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = appName, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = rating, color = Color.Gray, fontSize = 12.sp)
                Text(text = sales, color = Color.Gray, fontSize = 12.sp)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go to details",
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(navController = rememberNavController())
}
