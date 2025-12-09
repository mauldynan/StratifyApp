package com.example.stratify.view.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.R // Pastikan import R sesuai package kamu
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ProfileOptionsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClicked: () -> Unit, // Navigasi ke Edit Profile
    onLogoutClicked: () -> Unit // Logika Logout
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Menggunakan SharedViewModel (Opsional, bisa pakai currentUser langsung)
    // Jika SharedViewModel error saat dipanggil di sini, bisa dihapus dan pakai data Firebase langsung
    val sharedViewModel: SharedViewModel = viewModel()

    // State Data User
    val displayName by sharedViewModel.displayName.observeAsState(currentUser?.displayName ?: "No Name")
    val photoUri by sharedViewModel.photoUri.observeAsState(currentUser?.photoUrl)
    val email = currentUser?.email ?: "No Email"

    // State untuk Dropdown Menu
    var isAccountExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile Options", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState()) // Agar bisa di-scroll
        ) {

            // --- HEADER PROFILE ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto Profil (Glide)
                GlideImage(
                    model = photoUri ?: R.drawable.ic_profile, // Fallback drawable
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                ) {
                    it.placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .circleCrop()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nama & Email
                Text(
                    text = displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = email,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Divider(thickness = 1.dp, color = Color.LightGray)

            // --- MENU OPTIONS ---

            // 1. Edit Profile
            OptionItem(
                text = "Edit Profile",
                onClick = onEditProfileClicked
            )

            // 2. Account (Expandable)
            ExpandableOptionItem(
                text = "Account",
                isExpanded = isAccountExpanded,
                onToggle = { isAccountExpanded = !isAccountExpanded }
            ) {
                // Sub-menu Account: LOGOUT
                OptionSubItem(
                    text = "Logout",
                    onClick = onLogoutClicked,
                    textColor = Color.Red // Merah biar kelihatan tombol danger
                )
            }

            // 3. Language (Expandable)
            ExpandableOptionItem(
                text = "Language",
                isExpanded = isLanguageExpanded,
                onToggle = { isLanguageExpanded = !isLanguageExpanded }
            ) {
                // Sub-menu Language
                OptionSubItem(text = "English", onClick = { /* Logic Ganti Bahasa */ })
                OptionSubItem(text = "Indonesia", onClick = { /* Logic Ganti Bahasa */ })
            }
        }
    }
}

// --- KOMPONEN HELPER (Biar kodenya rapi) ---

@Composable
fun OptionItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        // Bisa tambah icon panah kanan kalau mau
    }
    Divider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 24.dp))
}

@Composable
fun ExpandableOptionItem(
    text: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        // Tampilkan konten sub-menu jika expanded
        if (isExpanded) {
            content()
        }

        Divider(thickness = 0.5.dp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 24.dp))
    }
}

@Composable
fun OptionSubItem(
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(Color(0xFFF5F5F5)) // Background agak abu untuk sub-item
            .padding(start = 48.dp, end = 24.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Text(text = text, fontSize = 14.sp, color = textColor)
    }
}