package com.example.stratify.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
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
import com.google.firebase.auth.FirebaseAuth
import com.example.stratify.compare.DashboardViewModel
import com.example.stratify.compare.Review


// --- Optimized Colors ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val positiveGreen = Color(0xFF10B981)
private val negativeRed = Color(0xFFEF4444)
private val lightBg = Color(0xFFF8F9FB)

// Data class untuk database simulasi
data class EcommerceAppData(
    val id: Int,
    val name: String,
    val firebaseKey: String,
    val logo: Int,
    val rating: String,
    val reviews: String,
    val pos: Int,
    val neg: Int,
    val latestComments: List<Pair<String, String>> // Nama User dan Komentar
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun DashboardScreen(navController: NavController, sharedViewModel: SharedViewModel = viewModel()) {
    // FIX: Removed local drawer state, use Navigation instead
    val isPreview = LocalInspectionMode.current
    val initialPhotoUrl = if (isPreview) null else FirebaseAuth.getInstance().currentUser?.photoUrl
    val photoUri by sharedViewModel.photoUri.observeAsState(initialPhotoUrl)

    // State untuk fitur pencarian
    var searchQuery by remember { mutableStateOf("") }

    // State untuk Pin dan Compare
    var savedAppId by remember { mutableStateOf<Int?>(1) }
    var compareA by remember { mutableStateOf<EcommerceAppData?>(null) }
    var compareB by remember { mutableStateOf<EcommerceAppData?>(null) }
    var isCompareViewActive by remember { mutableStateOf(false) }

    // Mock Database terintegrasi
    val ecommerceApps = remember {
        listOf(
            EcommerceAppData(1, "Shopee", "shopee", R.drawable.shopee_logo, "4.8", "1.2M", 850, 150, listOf(
                "Budi S." to "Love the free shipping vouchers!",
                "Siti A." to "Items arrived fast and as described.",
                "Andi W." to "App feels a bit laggy sometimes."
            )),
            EcommerceAppData(2, "Tokopedia", "tokopedia", R.drawable.tokopedia_logo, "4.7", "900K", 780, 220, listOf(
                "Rina K." to "Everything is original in Official Stores.",
                "Dedi H." to "Customer service is very responsive.",
                "Lina M." to "Update frequency is a bit too high."
            )),
            EcommerceAppData(3, "Tiktok", "tiktok",R.drawable.tiktok_logo, "4.5", "600K", 600, 400, listOf(
                "Eko P." to "Great monthly discounts available.",
                "Ani R." to "Very secure packaging.",
                "Zaki F." to "Shipping takes too long."
            ))
        )
    }

    val filteredApps = ecommerceApps.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = maroonPrimary),
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_stratify3),
                                contentDescription = "Logo",
                                modifier = Modifier.height(38.dp).padding(start = 20.dp)
                            )
                            IconButton(onClick = { navController.navigate(Screen.ProfileOptions.route) }, modifier = Modifier.padding(end = 8.dp)) {
                                if (isPreview) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_profile_placeholder),
                                        contentDescription = "Profile",
                                        modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, goldAccent, CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    GlideImage(
                                        model = photoUri,
                                        contentDescription = "Profile",
                                        modifier = Modifier.size(32.dp).clip(CircleShape).border(1.dp, goldAccent, CircleShape),
                                        contentScale = ContentScale.Crop
                                    ) { it.error(R.drawable.ic_profile_placeholder).placeholder(R.drawable.ic_profile_placeholder) }
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
                    .background(lightBg)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (isCompareViewActive && compareA != null && compareB != null) {
                    // --- TAMPILAN PERBANDINGAN ---
                    CompareViewContent(
                        appA = compareA!!,
                        appB = compareB!!,
                        onBack = {
                            isCompareViewActive = false
                            compareA = null
                            compareB = null
                        }
                    )
                } else {
                    // --- TAMPILAN UTAMA ---

                    // Banner Seleksi Compare
                    if (compareA != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = goldAccent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = maroonPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Comparing with ${compareA?.name}. Select another!",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = maroonPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { compareA = null }, modifier = Modifier.size(20.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = maroonPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Pinned Section
                    if (savedAppId != null) {
                        val pinned = ecommerceApps.find { it.id == savedAppId }
                        pinned?.let {
                            PinnedAppCard(
                                appName = it.name,
                                posPercent = it.pos * 100 / (it.pos + it.neg),
                                negPercent = it.neg * 100 / (it.pos + it.neg),
                                totalReviews = it.pos + it.neg,
                                onDetailClick = { navController.navigate(Screen.FullAnalysis.route) }
                            )
                        }
                    } else {
                        EmptyPinnedState()
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "E-Commerce List",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = maroonPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    filteredApps.forEach { app ->
                        EcommerceListItem(
                            appData = app,
                            isSaved = savedAppId == app.id,
                            isSelectingForCompare = compareA?.id == app.id,
                            onSaveToggle = { savedAppId = if (savedAppId == app.id) null else app.id },
                            onCompareClick = {
                                val fullApp = ecommerceApps.first { it.id == app.id }

                                if (compareA == null) {
                                    compareA = fullApp
                                } else if (compareA?.id != fullApp.id) {
                                    compareB = fullApp
                                    isCompareViewActive = true
                                }
                            }

                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompareViewContent(
    appA: EcommerceAppData,
    appB: EcommerceAppData,
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {

    // 🔥 LOAD FIREBASE SEKALI
    LaunchedEffect(Unit) {
        viewModel.loadReviews()
    }

    // 🔥 AMBIL DATA SESUAI APP
    val reviewsA =
        if (appA.name == "Shopee") viewModel.shopeeReviews.value
        else viewModel.tokopediaReviews.value

    val reviewsB =
        if (appB.name == "Shopee") viewModel.shopeeReviews.value
        else viewModel.tokopediaReviews.value

    Column(modifier = Modifier.fillMaxWidth()) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Image(painterResource(appA.logo), null, Modifier.size(50.dp))
                        Text(appA.name, fontWeight = FontWeight.Black, color = maroonPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(maroonPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("VS", color = goldAccent, fontWeight = FontWeight.Black)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Image(painterResource(appB.logo), null, Modifier.size(50.dp))
                        Text(appB.name, fontWeight = FontWeight.Black, color = maroonPrimary)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // RATING
                Text(
                    "RATING & REVIEWS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = lightBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(appA.rating, fontSize = 26.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
                            Text(appA.reviews, fontSize = 11.sp, color = Color.Gray)
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(Color.LightGray)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(appB.rating, fontSize = 26.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
                            Text(appB.reviews, fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // LATEST REVIEWS
                Text(
                    "LATEST REVIEWS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        reviewsA.forEach {
                            CompareCommentItem(it.userName, it.content)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        reviewsB.forEach {
                            CompareCommentItem(it.userName, it.content)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("BACK TO LIST", fontWeight = FontWeight.Black, color = goldAccent)
        }
    }
}



@Composable
fun CompareCommentItem(user: String, comment: String) {
    Card(colors = CardDefaults.cardColors(containerColor = lightBg), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(user, fontSize = 9.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
            Text("\"$comment\"", fontSize = 10.sp, color = Color.DarkGray, fontStyle = FontStyle.Italic, lineHeight = 12.sp)
        }
    }
}

@Composable
fun PinnedAppCard(appName: String, posPercent: Int, negPercent: Int, totalReviews: Int, onDetailClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(170.dp), shape = RoundedCornerShape(32.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(color = goldAccent, shape = RoundedCornerShape(50)) {
                    Text("Pinned App", fontSize = 9.sp, fontWeight = FontWeight.Black, color = maroonPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
                Spacer(Modifier.height(4.dp))
                Text(text = "$appName Summary", fontSize = 18.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(positiveGreen))
                    Text(" $posPercent% Positive", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = positiveGreen)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(6.dp).clip(CircleShape).background(negativeRed))
                    Text(" $negPercent% Negative", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = negativeRed)
                }
                Button(onClick = onDetailClick, colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary), shape = RoundedCornerShape(12.dp), modifier = Modifier.height(32.dp)) {
                    Text("Detail", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = goldAccent)
                }
            }
            DonutChart(modifier = Modifier.size(90.dp), positivePercent = posPercent.toFloat(), negativePercent = negPercent.toFloat(), totalReviews = totalReviews)
        }
    }
}

@Composable
fun EmptyPinnedState() {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(maroonPrimary.copy(alpha = 0.05f)).border(1.dp, maroonPrimary.copy(0.1f), RoundedCornerShape(32.dp)).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(goldAccent.copy(0.2f)), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = goldAccent
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("No Data Tracked", fontSize = 10.sp, fontWeight = FontWeight.Black, color = goldAccent)
        Spacer(Modifier.height(4.dp))
        Text("No Pinned App Yet", fontSize = 18.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Pin your favorite e-commerce below to see its detailed sentiment summary here at a glance!", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.Gray, lineHeight = 16.sp)
    }
}

@Composable
fun EcommerceListItem(appData: EcommerceAppData, isSaved: Boolean, isSelectingForCompare: Boolean, onSaveToggle: () -> Unit, onCompareClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val borderColor by animateColorAsState(if (isPressed || isSaved || isSelectingForCompare) goldAccent else Color.Transparent)

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).border(2.dp, borderColor, RoundedCornerShape(24.dp)).clickable(interactionSource = interactionSource, indication = null, onClick = { }), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 4.dp else 0.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(54.dp), shape = RoundedCornerShape(16.dp), color = lightBg) {
                Image(painter = painterResource(id = appData.logo), contentDescription = null, modifier = Modifier.padding(10.dp), contentScale = ContentScale.Fit)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = appData.name, fontSize = 16.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = goldAccent
                    )
                    Text(" ${appData.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Text(" | ${appData.reviews} Reviews", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCompareClick, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSelectingForCompare) goldAccent.copy(0.2f) else Color.Transparent)) {
                    Icon(
                        imageVector = Icons.Outlined.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelectingForCompare) goldAccent else Color.LightGray
                    )
                }
                IconButton(onClick = onSaveToggle, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSaved) maroonPrimary else lightBg)) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSaved) goldAccent else Color.LightGray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    val viewModel = SharedViewModel()
    DashboardScreen(navController = rememberNavController(), sharedViewModel = viewModel)
}
