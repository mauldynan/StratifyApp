package com.example.stratify.view.profile

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.R // PASTIKAN INI SESUAI DENGAN PACKAGE PROJECT KAMU
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

// Konstanta Cloudinary
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
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser

    // --- STATE UI ---
    var name by remember { mutableStateOf(currentUser?.displayName ?: "") }

    // Model Gambar untuk Glide
    var displayedImageModel by remember { mutableStateOf<Any?>(currentUser?.photoUrl) }

    // Uri file baru yang akan diupload
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Temp URI untuk Kamera
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // --- LAUNCHERS ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            newImageUri = it
            displayedImageModel = it
            sharedViewModel.photoUri.value = it
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            newImageUri = tempCameraUri
            displayedImageModel = tempCameraUri
            sharedViewModel.photoUri.value = tempCameraUri
        }
    }

    // --- UI SCREEN ---
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color(0xFF800000), // Maroon
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        // FIX: Menggunakan AutoMirrored untuk menghilangkan warning deprecated
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF800000)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // --- FOTO PROFIL (GLIDE) ---
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.size(120.dp)
                ) {
                    GlideImage(
                        model = displayedImageModel ?: R.drawable.ic_profile,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                            .clickable(enabled = !isLoading) { showDialog = true }
                    ) {
                        it.placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                    }

                    // Icon Edit
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.Gray, CircleShape)
                            .clickable(enabled = !isLoading) { showDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- INPUT NAMA ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.weight(1f))

                // --- TOMBOL SAVE ---
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            Toast.makeText(context, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        } else {
                            scope.launch {
                                isLoading = true

                                if (newImageUri != null) {
                                    uploadImageToCloudinary(context, newImageUri!!, name) { success, url ->
                                        if (success) {
                                            onProfileUpdated(name, url)
                                            sharedViewModel.displayName.value = name
                                            Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                            onNavigateBack()
                                        }
                                        isLoading = false
                                    }
                                } else {
                                    updateFirebaseProfile(name, null) { success ->
                                        if (success) {
                                            onProfileUpdated(name, null)
                                            sharedViewModel.displayName.value = name
                                            Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                            onNavigateBack()
                                        } else {
                                            Toast.makeText(context, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                                        }
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF800000)
                    )
                ) {
                    Text(text = if (isLoading) "Uploading..." else "Save Changes")
                }
            }

            // --- LOADING INDICATOR ---
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // --- DIALOG PILIH FOTO ---
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Pilih Foto Profil") },
                    text = {
                        Column {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    val uri = createTempImageUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ambil Foto (Kamera)", color = Color.Black)
                            }

                            TextButton(
                                onClick = {
                                    showDialog = false
                                    galleryLauncher.launch("image/*")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pilih dari Galeri", color = Color.Black)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Batal", color = Color.Red)
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
//    HELPER FUNCTIONS (Dibuat PRIVATE)
// ==========================================

// Private agar tidak bentrock dengan file lain
private fun createTempImageUri(context: Context): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

private suspend fun uploadImageToCloudinary(
    context: Context,
    uri: Uri,
    newName: String,
    onResult: (Boolean, String?) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val file = getFileFromUri(context, uri)
            if (file == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Gagal membaca file gambar", Toast.LENGTH_SHORT).show()
                    onResult(false, null)
                }
                return@withContext
            }

            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, requestBody)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseData = response.body?.string()

            val imageUrl = Regex("\"secure_url\":\"(.*?)\"")
                .find(responseData ?: "")?.groupValues?.get(1)?.replace("\\/", "/")

            withContext(Dispatchers.Main) {
                if (imageUrl != null) {
                    updateFirebaseProfile(newName, imageUrl) { success ->
                        onResult(success, imageUrl)
                    }
                } else {
                    Toast.makeText(context, "Gagal upload ke Cloud server", Toast.LENGTH_SHORT).show()
                    onResult(false, null)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                onResult(false, null)
            }
        }
    }
}

private fun updateFirebaseProfile(name: String, photoUrl: String?, onComplete: (Boolean) -> Unit) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onComplete(false)
        return
    }

    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(name)
        .apply {
            if (photoUrl != null) setPhotoUri(Uri.parse(photoUrl))
        }.build()

    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
        onComplete(task.isSuccessful)
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_temp", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}