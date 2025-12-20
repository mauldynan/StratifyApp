package com.example.stratify.view.user

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.R
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

// --- Colors ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val negativeRed = Color(0xFFEF4444)
private val lightGrayBg = Color(0xFFF8F9FB)

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileOptionsScreen(
    onNavigateBack: () -> Unit,
    onEditProfileClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    sharedViewModel: SharedViewModel = viewModel()
) {
    val isPreview = LocalInspectionMode.current
    val currentUser = if (isPreview) null else FirebaseAuth.getInstance().currentUser

    val displayName by sharedViewModel.displayName.observeAsState(currentUser?.displayName ?: "User_Stratify")
    val photoUri by sharedViewModel.photoUri.observeAsState(currentUser?.photoUrl)
    val email = currentUser?.email ?: "user@stratify.com"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onNavigateBack() },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Sheet Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp))
                .background(Color.White)
                .padding(32.dp)
                .clickable(enabled = false) { } // Prevent closing when clicking content
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(48.dp, 6.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture with Gold Border
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(4.dp, goldAccent),
                    color = lightGrayBg
                ) {
                    if (isPreview) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        GlideImage(
                            model = photoUri ?: R.drawable.ic_profile,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        ) {
                            it.error(R.drawable.ic_profile)
                                .placeholder(R.drawable.ic_profile)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User Info
            Text(
                text = displayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = maroonPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Menu Items
            ProfileMenuItem(
                icon = Icons.Default.Person,
                label = "Edit Profile",
                onClick = onEditProfileClicked,
                color = maroonPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileMenuItem(
                icon = Icons.Default.ExitToApp,
                label = "Logout",
                onClick = onLogoutClicked,
                color = negativeRed,
                isNegative = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Close Button
            Text(
                text = "CLOSE MENU",
                modifier = Modifier
                    .clickable { onNavigateBack() }
                    .padding(12.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color,
    isNegative: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNegative) color.copy(alpha = 0.08f) else maroonPrimary.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Background Box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isNegative) Color.White else goldAccent
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = label.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isNegative) color else Color.Black,
                    letterSpacing = 0.5.sp
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isNegative) color.copy(alpha = 0.4f) else Color.LightGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileOptionsScreenPreview() {
    ProfileOptionsScreen(
        onNavigateBack = {},
        onEditProfileClicked = {},
        onLogoutClicked = {}
    )
}