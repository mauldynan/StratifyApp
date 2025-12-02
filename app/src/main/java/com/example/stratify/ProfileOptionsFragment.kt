package com.example.stratify

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileOptionsFragment : Fragment() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ProfileOptionsScreen(
                        sharedViewModel = sharedViewModel,
                        onEditProfileClicked = {
                            findNavController().navigate(R.id.profileEditFragment)
                        },
                        onLogoutClicked = {
                            auth.signOut()
                            findNavController().navigate(R.id.action_global_loginActivity)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileOptionsScreen(
    sharedViewModel: SharedViewModel,
    onEditProfileClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val displayName by sharedViewModel.displayName.observeAsState(user?.displayName ?: "User Name")
    val photoUri by sharedViewModel.photoUri.observeAsState(user?.photoUrl)
    val email = user?.email ?: "user.email@example.com"

    var accountOptionsVisible by remember { mutableStateOf(false) }
    var languageOptionsVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Profile Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            GlideImage(
                model = photoUri,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            ) {
                it.placeholder(R.drawable.ic_person)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = displayName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(text = email, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Options
        ProfileOptionItem("Edit Profile", onEditProfileClicked)
        ProfileOptionItem(
            "Account",
            onClick = { accountOptionsVisible = !accountOptionsVisible },
            showDropdown = true
        )
        if (accountOptionsVisible) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                ProfileOptionItem("Logout", onLogoutClicked)
            }
        }

        ProfileOptionItem(
            "Language",
            onClick = { languageOptionsVisible = !languageOptionsVisible },
            showDropdown = true
        )
        if (languageOptionsVisible) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                ProfileOptionItem("English") { /* TODO */ }
                ProfileOptionItem("Indonesia") { /* TODO */ }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    text: String,
    onClick: () -> Unit,
    showDropdown: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (showDropdown) {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
    }
}
