package com.example.stratify

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.stratify.databinding.FragmentProfileEditBinding
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var imageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val CLOUD_NAME = "dipoxvy4b"
    private val UPLOAD_PRESET = "android_upload_cloudinary"
    private val PICK_IMAGE = 1001
    private val TAKE_PHOTO = 1002

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = firebaseAuth.currentUser
        user?.let {
            binding.etName.setText(it.displayName ?: "")
            it.photoUrl?.let { uri -> Picasso.get().load(uri).into(binding.ivProfileImageEdit) }
        }

        binding.ivProfileImageEdit.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnSaveChanges.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (imageUri != null) {
                uploadImageToCloudinary(imageUri!!, newName)
            } else {
                updateFirebaseProfile(newName, null)
            }
        }
    }

    private fun showImagePickerDialog() {
        val hasCamera = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

        val options = if (hasCamera) {
            arrayOf("Ambil Foto", "Pilih dari Galeri")
        } else {
            arrayOf("Pilih dari Galeri")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Foto Profil")
            .setItems(options) { _, which ->
                if (hasCamera) {
                    when (which) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                } else {
                    openGallery()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun openCamera() {
        try {
            val photoFile = File(requireContext().cacheDir, "camera_photo_${System.currentTimeMillis()}.jpg")

            cameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(intent, TAKE_PHOTO)
            } else {
                Toast.makeText(requireContext(), "Tidak ada aplikasi kamera", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        showLoading(true, "Memuat gambar...")
                        loadImageAsync(uri)
                    }
                }
            }
            TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    cameraImageUri?.let { uri ->
                        showLoading(true, "Memuat foto...")
                        loadImageAsync(uri)
                    }
                }
            }
        }
    }

    private fun loadImageAsync(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Simulasi loading (opsional)
                delay(300)

                withContext(Dispatchers.Main) {
                    imageUri = uri
                    binding.ivProfileImageEdit.setImageURI(uri)
                    showLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean, message: String = "Uploading...") {
        binding.btnSaveChanges.isEnabled = !isLoading
        binding.btnSaveChanges.text = if (isLoading) message else "Save Changes"
        binding.ivProfileImageEdit.isEnabled = !isLoading

        // Tampilkan/sembunyikan ProgressBar
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun uploadImageToCloudinary(uri: Uri, name: String) {
        val context = requireContext()

        // Tampilkan loading
        showLoading(true, "Uploading...")

        val file = File(getRealPathFromURI(uri))
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestBody)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
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
                    showLoading(false)

                    if (imageUrl != null) {
                        updateFirebaseProfile(name, imageUrl)
                    } else {
                        Toast.makeText(context, "Gagal upload ke Cloudinary", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFirebaseProfile(name: String, photoUrl: String?) {
        val user = firebaseAuth.currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .apply {
                if (photoUrl != null) setPhotoUri(Uri.parse(photoUrl))
            }.build()

        user.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                sharedViewModel.displayName.value = name
                if (photoUrl != null) {
                    sharedViewModel.photoUri.value = Uri.parse(photoUrl)
                }

                parentFragmentManager.setFragmentResult(
                    "profile_updated",
                    Bundle().apply {
                        putString("name", name)
                        putString("photoUrl", photoUrl)
                    }
                )

                Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile.absolutePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}