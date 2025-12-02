package com.example.stratify

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileEditFragment : Fragment() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val CLOUD_NAME = "dipoxvy4b"
    private val UPLOAD_PRESET = "android_upload_cloudinary"

    private var cameraImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val user = firebaseAuth.currentUser
                    ProfileEditScreen(
                        initialName = user?.displayName ?: "",
                        initialPhotoUrl = user?.photoUrl?.toString(),
                        onSaveChanges = { name, imageUri ->
                            if (name.isEmpty()) {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                            } else {
                                if (imageUri != null) {
                                    uploadImageToCloudinary(imageUri, name)
                                } else {
                                    updateFirebaseProfile(name, null)
                                }
                            }
                        },
                        onBackPressed = {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }

    private fun getTmpFileUri(): Uri {
        val context = requireContext()
        val tmpFile = File.createTempFile("camera_photo_", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        cameraImageUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tmpFile
        )
        return cameraImageUri!!
    }

    private fun uploadImageToCloudinary(uri: Uri, name: String) {
        // ... (upload logic remains the same)
    }

    private fun updateFirebaseProfile(name: String, photoUrl: String?) {
        // ... (update logic remains the same)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        // ... (get real path logic remains the same)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileEditScreen(
    initialName: String,
    initialPhotoUrl: String?,
    onSaveChanges: (name: String, imageUri: Uri?) -> Unit,
    onBackPressed: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                // The URI is already in `cameraImageUri` from the fragment
                // We just need to trigger a recomposition
                imageUri = Uri.parse(imageUri.toString() + "?t=" + System.currentTimeMillis()) // Force recomposition

            }
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Choose Profile Photo") },
            text = { Text("Select an option") },
            confirmButton = {
                TextButton(onClick = {
                    cameraLauncher.launch( (context as ProfileEditFragment).getTmpFileUri() )
                    showDialog = false
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showDialog = false
                }) {
                    Text("Gallery")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Top Bar
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackPressed) {
                Icon(painterResource(id = R.drawable.ic_arrow_back), contentDescription = "Back")
            }
            Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Image
        GlideImage(
            model = imageUri ?: initialPhotoUrl,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { showDialog = true },
            contentScale = ContentScale.Crop
        ) {
            it.placeholder(R.drawable.ic_person)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = { onSaveChanges(name, imageUri) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Save Changes")
            }
        }
    }
}