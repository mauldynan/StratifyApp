package com.example.stratify

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stratify.databinding.FragmentProfileEditBinding
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File


class ProfileEditFragment : Fragment() {

    // View binding for the fragment's layout.
    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    // URI of the image selected by the user (from gallery or camera).
    private var selectedImageUri: Uri? = null
    // URI for the image captured by the camera.
    private var cameraImageUri: Uri? = null

    // Shared ViewModel to hold and share user profile data across fragments.
    private val sharedViewModel: SharedViewModel by activityViewModels()

    // Activity Result Launcher for handling results from other activities,
    // like the camera or gallery image pickers.
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Get the URI from the gallery intent or use the camera URI.
                val galleryUri = result.data?.data
                selectedImageUri = galleryUri ?: cameraImageUri

                // Load the selected image into the ImageView.
                selectedImageUri?.let {
                    Glide.with(this)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .circleCrop()
                        .into(binding.ivProfileImageEdit)
                }
            }
        }

    // Activity Result Launcher for requesting camera permission.
    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // If permission is granted, launch the camera.
                launchCamera()
            } else {
                // Otherwise, inform the user that permission is required.
                Toast.makeText(
                    requireContext(),
                    "Camera permission is required to take photos.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Initializes views, observers, and click listeners after the view has been created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        loadUserData()
        observeViewModel()
        setupClickListeners()
    }

    /**
     * Observes the SharedViewModel for changes in user data (name and photo URI)
     * and updates the UI accordingly.
     */
    private fun observeViewModel() {
        sharedViewModel.displayName.observe(viewLifecycleOwner) { name ->
            binding.etName.setText(name)
        }
        sharedViewModel.photoUri.observe(viewLifecycleOwner) { uri ->
            Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .into(binding.ivProfileImageEdit)
        }
    }

    /**
     * Loads the initial user data from the SharedViewModel or Firebase Auth
     * and populates the UI fields.
     */
    private fun loadUserData() {
        val user = auth.currentUser ?: return

        // Populate ViewModel from Firebase if it's empty.
        if (sharedViewModel.displayName.value == null && user.displayName != null) {
            sharedViewModel.displayName.value = user.displayName
        }
        if (sharedViewModel.photoUri.value == null && user.photoUrl != null) {
            sharedViewModel.photoUri.value = user.photoUrl
        }

        // Get initial data from ViewModel, falling back to Firebase user data.
        val initialName = sharedViewModel.displayName.value ?: user.displayName
        val initialPhotoUri = sharedViewModel.photoUri.value ?: user.photoUrl

        binding.etName.setText(initialName)

        // Set a placeholder drawable for the profile image.
        val placeholderResId = resources.getIdentifier("ic_person", "drawable", requireContext().packageName)

        Glide.with(this)
            .load(initialPhotoUri)
            .placeholder(placeholderResId.takeIf { it != 0 } ?: R.drawable.ic_person)
            .circleCrop()
            .into(binding.ivProfileImageEdit)

        // Set the initial selected image URI.
        selectedImageUri = initialPhotoUri
    }

    /**
     * Sets up click listeners for the back button, edit photo icon, and save changes button.
     */
    private fun setupClickListeners() = with(binding) {
        ivBack.setOnClickListener { findNavController().navigateUp() }
        ivEditPhotoIcon.setOnClickListener { showPhotoOptionsDialog() }
        btnSaveChanges.setOnClickListener { saveChanges() }
    }

    /**
     * Displays a dialog with options to either take a new photo or choose one from the gallery.
     */
    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) // Request camera permission
                    1 -> launchGallery() // Launch the gallery picker
                }
            }
            .show()
    }

    /**
     * Launches an intent to pick an image from the device's external storage (gallery).
     */
    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intent)
    }

    /**
     * Launches the device's camera app to take a new photo.
     * The captured image is saved to a temporary file.
     */
    private fun launchCamera() {
        val timeStamp = System.currentTimeMillis()
        val photoFile = File(requireContext().externalCacheDir, "IMG_$timeStamp.jpg")
        // Create a content URI for the temporary file using FileProvider.
        cameraImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        }
        activityResultLauncher.launch(intent)
    }

    /**
     * Persists the selected image URI to the app's internal storage.
     * This is crucial because URIs from the gallery or camera might be temporary.
     * @param sourceUri The temporary URI of the selected image.
     * @return A new, permanent URI for the image saved in the app's local storage, or null if saving fails.
     */
    private fun persistImageUri(sourceUri: Uri): Uri? {
        val timeStamp = System.currentTimeMillis()
        val destinationFile = File(requireContext().filesDir, "profile_pic_$timeStamp.jpg")

        return try {
            // Copy the image from the source URI to the destination file.
            requireContext().contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                destinationFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            // Return the URI of the newly created local file.
            Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save image locally.", Toast.LENGTH_SHORT).show()
            null
        }
    }

    /**
     * Saves the edited name and profile picture to the SharedViewModel.
     * It also persists the image locally if a new one was selected.
     */
    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        // Validate that the name is not empty.
        if (name.isEmpty()) {
            binding.tilName.error = "Name cannot be empty"
            return
        }

        binding.tilName.error = null // Clear any previous errors.
        sharedViewModel.displayName.value = name // Update the name in the ViewModel.

        selectedImageUri?.let { uri ->
            // Check if the URI is a local file or content URI that needs to be persisted.
            if (uri.scheme == "content" || uri.scheme == "file" || uri.scheme == null) {
                val persistedUri = persistImageUri(uri)
                persistedUri?.let {
                    // Update the photo URI in the ViewModel with the permanent URI.
                    sharedViewModel.photoUri.value = it
                }
            } else {
                // If it's a remote URL (e.g., from Firebase), use it directly.
                sharedViewModel.photoUri.value = uri
            }
        }

        Toast.makeText(context, "Changes updated locally", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp() // Navigate back to the previous screen.
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
