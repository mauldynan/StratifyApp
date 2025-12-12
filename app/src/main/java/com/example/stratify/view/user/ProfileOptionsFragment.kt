package com.example.stratify.view.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.R
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ProfileOptionsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val sharedViewModel: SharedViewModel = viewModel()

    val displayName by sharedViewModel.displayName.observeAsState(currentUser?.displayName ?: "No Name")
    val photoUri by sharedViewModel.photoUri.observeAsState(currentUser?.photoUrl)
    val email = currentUser?.email ?: "No Email"

    var isAccountExpanded by remember { mutableStateOf(false) }
    var isLanguageExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x55000000))
    ){

        // DRAWER KANAN
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.80f)
                .align(Alignment.CenterEnd)
                .background(
                    Color.White,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        bottomStart = 20.dp
                    )
                )
                .verticalScroll(rememberScrollState())
        ) {

            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF8B0000)
                    )
                }

                Text(
                    text = "Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF8B0000)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PROFILE AREA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                GlideImage(
                    model = photoUri ?: R.drawable.ic_profile,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B0000)
                )
                Text(
                    email,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFF8B0000), thickness = 1.dp)

            // MENU
            MenuItem(
                text = "Profile Edit",
                icon = R.drawable.ic_edit,
                onClick = onEditProfileClicked
            )

            ExpandMenuItem(
                text = "Account",
                icon = R.drawable.ic_account,
                isExpanded = isAccountExpanded,
                onToggle = { isAccountExpanded = !isAccountExpanded }
            ) {
                SubMenuItem(
                    text = "Log Out",
                    icon = R.drawable.ic_logout,
                    textColor = Color(0xFF8B0000),
                    onClick = onLogoutClicked
                )
            }

            ExpandMenuItem(
                text = "Language",
                icon = R.drawable.ic_language,
                isExpanded = isLanguageExpanded,
                onToggle = { isLanguageExpanded = !isLanguageExpanded }
            ) {
                SubMenuItem(text = "English", icon = null, onClick = {})
                SubMenuItem(text = "Indonesia", icon = null, onClick = {})
            }
        }
    }
}

@Composable
fun MenuItem(text: String, icon: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(id = icon),
            contentDescription = null,
            tint = Color(0xFF8B0000)
        )
        Spacer(Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
    Divider(color = Color.LightGray, thickness = 1.dp)
}

@Composable
fun ExpandMenuItem(
    text: String,
    icon: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = icon),
                contentDescription = null,
                tint = Color(0xFF8B0000)
            )
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF8B0000)
            )
        }
        if (isExpanded) content()
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}

@Composable
fun SubMenuItem(
    text: String,
    icon: Int?,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F8F8))
            .clickable { onClick() }
            .padding(start = 56.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = icon),
                contentDescription = null,
                tint = textColor
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(text, fontSize = 15.sp, color = textColor)
    }
}
