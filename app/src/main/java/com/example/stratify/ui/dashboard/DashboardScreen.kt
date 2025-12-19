package com.example.stratify.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.DonutChart
import com.example.stratify.R
import com.example.stratify.Screen
import com.example.stratify.view.profile.SharedViewModel
import com.example.stratify.view.user.ProfileOptionsScreen
import com.google.firebase.auth.FirebaseAuth

// --- Colors ---
private val maroonPrimary = Color(0xFF800000)
private val goldSearch = Color(0xFFF6C761)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun DashboardScreen(navController: NavController, sharedViewModel: SharedViewModel = viewModel()) {
    var showProfileDrawer by remember { mutableStateOf(false) }

    // Fix Render Issue: Hindari pemanggilan FirebaseAuth saat Preview
    val isPreview = LocalInspectionMode.current
    val initialPhotoUrl = if (isPreview) null else FirebaseAuth.getInstance().currentUser?.photoUrl

    val photoUri by sharedViewModel.photoUri.observeAsState(initialPhotoUrl)

    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = maroonPrimary
                    ),
                    actions = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Logo Stratify
                            Image(
                                painter = painterResource(id = R.drawable.logo_stratify3),
                                contentDescription = "Stratify Logo",
                                modifier = Modifier
                                    .height(38.dp)
                                    .padding(start = 12.dp)
                            )

                            // Search Bar Kustom (Transparent Background + Gold Theme)
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .height(32.dp)
                                    .background(Color.Transparent, RoundedCornerShape(50))
                                    .border(1.dp, goldSearch, RoundedCornerShape(50)),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 12.sp,
                                    color = goldSearch
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search Icon",
                                            modifier = Modifier.size(16.dp),
                                            tint = goldSearch
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search",
                                                    fontSize = 12.sp,
                                                    color = goldSearch.copy(alpha = 0.7f)
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )

                            // Profile Button
                            IconButton(
                                onClick = { showProfileDrawer = true },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                GlideImage(
                                    model = photoUri,
                                    contentDescription = "Account",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, goldSearch, CircleShape), // Tambahan border emas tipis agar match
                                    contentScale = ContentScale.Crop
                                ) {
                                    it.error(R.drawable.ic_profile_placeholder)
                                        .placeholder(R.drawable.ic_profile_placeholder)
                                }
                            }
                        }
                    }
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

                Spacer(modifier = Modifier.height(12.dp))

                MonitoredAppCard(navController = navController)

                Spacer(modifier = Modifier.height(24.dp))

                SectionHeader("Recently Viewed Apps")
                Spacer(modifier = Modifier.height(8.dp))
                RecentlyViewedApps()

                Spacer(modifier = Modifier.height(24.dp))

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

        if (showProfileDrawer) {
            ProfileOptionsScreen(
                onNavigateBack = { showProfileDrawer = false },
                onEditProfileClicked = {
                    showProfileDrawer = false
                    navController.navigate(Screen.EditProfile.route)
                },
                onLogoutClicked = { /* TODO */ }
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    .padding(start = 12.dp),
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.padding(start = 8.dp, end = 12.dp)
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
                modifier = Modifier.padding(end = 12.dp),
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
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary)
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
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        )
    } else if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD))
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
                tint = Color.LightGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardScreen(navController = rememberNavController())
}
