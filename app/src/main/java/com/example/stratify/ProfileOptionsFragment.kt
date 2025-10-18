package com.example.stratify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.stratify.databinding.FragmentProfileOptionsBinding
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

class ProfileOptionsFragment : Fragment() {

    private var _binding: FragmentProfileOptionsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        loadUserData()
    }

    private fun setupClickListeners() = with(binding) {
        // Navigate to Edit Profile
        optionProfileEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileOptionsFragment_to_profileEditFragment)
        }

        // Toggle Account section
        optionAccount.setOnClickListener {
            accountOptionsContainer.isVisible = !accountOptionsContainer.isVisible
        }

        // Log Out
        optionLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_global_loginActivity)
        }

        // Toggle Language
        optionLanguage.setOnClickListener {
            languageOptionsContainer.isVisible = !languageOptionsContainer.isVisible
        }

        optionEnglish.setOnClickListener {
        }

        optionIndonesia.setOnClickListener {
        }
    }

    private fun loadUserData() {
        val displayName = sharedViewModel.displayName.value
        val photoUri = sharedViewModel.photoUri.value

        if (displayName != null && photoUri != null) {
            binding.tvProfileName.text = displayName
            Glide.with(this)
                .load(photoUri)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivProfileImage)
        } else {
            auth.currentUser?.let { user ->
                binding.tvProfileName.text = user.displayName ?: "User Name"
                binding.tvProfileEmail.text = user.email ?: "user.email@example.com"

                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.ivProfileImage)
            }
        }
    }


    private fun observeViewModel() = with(sharedViewModel) {
        displayName.observe(viewLifecycleOwner) { name ->
            binding.tvProfileName.text = name
        }

        photoUri.observe(viewLifecycleOwner) { uri ->
            Glide.with(this@ProfileOptionsFragment)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivProfileImage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
