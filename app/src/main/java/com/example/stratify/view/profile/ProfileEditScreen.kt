package com.example.stratify.view.profile

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Design Tokens ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)

// Cloudinary Constants
private const val CLOUD_NAME = "dipoxvy4b"
private const val UPLOAD_PRESET = "android_upload_cloudinary"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ProfileEditScreen(
    sharedViewModel: SharedViewModel,
    onNavigateBack: () -> Unit,
    onProfileUpdated: (String, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current
    val currentUser = if (isPreview) null else FirebaseAuth.getInstance().currentUser

    // --- STATE ---
    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }
    val email = currentUser?.email ?: "user@stratify.com"

    var displayedImageModel by remember { mutableStateOf<Any?>(currentUser?.photoUrl) }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // --- LAUNCHERS ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            newImageUri = it
            displayedImageModel = it
            sharedViewModel.photoUri.value = it
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            newImageUri = tempCameraUri
            displayedImageModel = tempCameraUri
            sharedViewModel.photoUri.value = tempCameraUri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "EDIT PROFILE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(end = 48.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = maroonPrimary)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(lightBg)
            .padding(paddingValues)) {

            // Main structure: Header & PP Fixed, Form Scrollable
            Column(modifier = Modifier.fillMaxSize()) {

                // --- FIXED SECTION ---
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Curved Header Decoration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                maroonPrimary,
                                shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                            )
                    )

                    // Profile Photo Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                                shadowElevation = 12.dp
                            ) {
                                if (isPreview) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_profile),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    @OptIn(ExperimentalGlideComposeApi::class)
                                    GlideImage(
                                        model = displayedImageModel ?: R.drawable.ic_profile,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    ) {
                                        it.placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile)
                                    }
                                }
                            }
                            // Camera Badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(goldAccent)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable(enabled = !isLoading) { showDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp), maroonPrimary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Change Profile Photo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.LightGray,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SCROLLABLE FORM SECTION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    EditInputField(
                        label = "USERNAME",
                        value = name,
                        onValueChange = { name = it },
                        icon = Icons.Default.Person,
                        enabled = !isLoading
                    )

                    EditInputField(
                        label = "Email Address",
                        value = email,
                        onValueChange = {},
                        icon = Icons.Default.Mail,
                        enabled = false,
                        trailingIcon = Icons.Default.Shield
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    isLoading = true
                                    if (newImageUri != null) {
                                        uploadImageToCloudinary(context, newImageUri!!, name) { success, url ->
                                            if (success) {
                                                onProfileUpdated(name, url)
                                                sharedViewModel.displayName.value = name
                                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                                onNavigateBack()
                                            }
                                            isLoading = false
                                        }
                                    } else {
                                        updateFirebaseProfile(name, null) { success ->
                                            if (success) {
                                                onProfileUpdated(name, null)
                                                sharedViewModel.displayName.value = name
                                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                                onNavigateBack()
                                            } else {
                                                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                            }
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text(
                            text = if (isLoading) "UPLOADING..." else "SAVE CHANGES",
                            fontWeight = FontWeight.Black,
                            color = goldAccent,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Photo Selection Dialog
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    title = {
                        Text(
                            text = "Select Profile Photo",
                            fontWeight = FontWeight.Black,
                            color = maroonPrimary,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    showDialog = false
                                    val uri = createTempImageUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhotoCamera, null, tint = maroonPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Text("Take Photo (Camera)", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    showDialog = false
                                    galleryLauncher.launch("image/*")
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, null, tint = maroonPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Text("Choose from Gallery", fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("CANCEL", fontWeight = FontWeight.Black, color = Color.Red)
                        }
                    }
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(0.3f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = goldAccent)
                }
            }
        }
    }
}

@Composable
fun EditInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = maroonPrimary.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(if (enabled) Color.White else Color(0xFFF1F2F4), RoundedCornerShape(16.dp)),
            leadingIcon = { Icon(icon, null, tint = if (enabled) Color.Gray else Color.LightGray, modifier = Modifier.size(20.dp)) },
            trailingIcon = if (trailingIcon != null) {
                { Icon(trailingIcon, null, tint = if (enabled) maroonPrimary else Color(0xFF10B981), modifier = Modifier.size(18.dp)) }
            } else null,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = goldAccent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedTextColor = maroonPrimary,
                unfocusedTextColor = maroonPrimary,
                disabledTextColor = Color.Gray
            ),
            textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        )
    }
}

// --- Helpers (Private) ---
private fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

private suspend fun uploadImageToCloudinary(context: Context, uri: Uri, newName: String, onResult: (Boolean, String?) -> Unit) {
    withContext(Dispatchers.IO) {
        try {
            val file = getFileFromUri(context, uri) ?: return@withContext onResult(false, null)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, requestBody)
                .addFormDataPart("upload_preset", UPLOAD_PRESET).build()
            val request = Request.Builder().url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload").post(body).build()
            val response = OkHttpClient().newCall(request).execute()
            val responseString = response.body?.string()
            val imageUrl = Regex("\"secure_url\":\"(.*?)\"").find(responseString ?: "")?.groupValues?.get(1)?.replace("\\/", "/")
            withContext(Dispatchers.Main) {
                if (imageUrl != null) updateFirebaseProfile(newName, imageUrl) { onResult(it, imageUrl) }
                else onResult(false, null)
            }
        } catch (e: Exception) { withContext(Dispatchers.Main) { onResult(false, null) } }
    }
}

private fun updateFirebaseProfile(name: String, photoUrl: String?, onComplete: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser ?: return onComplete(false)
    val updates = UserProfileChangeRequest.Builder().setDisplayName(name).apply {
        if (photoUrl != null) setPhotoUri(Uri.parse(photoUrl)!!)
    }.build()
    user.updateProfile(updates).addOnCompleteListener { onComplete(it.isSuccessful) }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_temp", ".jpg", context.cacheDir)
        inputStream?.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }
        tempFile
    } catch (e: Exception) { null }
}

@Preview(showBackground = true)
@Composable
fun ProfileEditScreenPreview() {
    ProfileEditScreen(
        sharedViewModel = SharedViewModel(),
        onNavigateBack = {},
        onProfileUpdated = { _, _ -> }
    )
}