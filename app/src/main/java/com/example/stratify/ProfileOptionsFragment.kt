package com.example.stratify

import android.os.Bundle
import android.util.Log
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
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // SharedViewModel yang sama dengan MainActivity & MainWorkspace
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadUserData()
        observeViewModel()
        listenForProfileUpdate()

        Log.d("DBG_PROFILE", "ProfileOptionsFragment onViewCreated")
    }

    private fun setupClickListeners() = with(binding) {
        optionProfileEdit.setOnClickListener {
            findNavController().navigate(R.id.profileEditFragment)
        }

        optionAccount.setOnClickListener {
            accountOptionsContainer.isVisible = !accountOptionsContainer.isVisible
        }

        optionLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_global_loginActivity)
        }

        optionLanguage.setOnClickListener {
            languageOptionsContainer.isVisible = !languageOptionsContainer.isVisible
        }

        optionEnglish.setOnClickListener {
            // TODO: implement language change
        }

        optionIndonesia.setOnClickListener {
            // TODO: implement language change
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        Log.d("DBG_PROFILE", "loadUserData: ${user?.displayName}, ${user?.photoUrl}")

        binding.tvProfileEmail.text = user?.email ?: "user.email@example.com"

        // Prefer dari ViewModel, fallback ke Firebase
        binding.tvProfileName.text = sharedViewModel.displayName.value
            ?: user?.displayName
                    ?: "User Name"

        val photoUri = sharedViewModel.photoUri.value ?: user?.photoUrl

        Glide.with(this)
            .load(photoUri)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(binding.ivProfileImage)
    }

    private fun observeViewModel() {
        // Observe perubahan dari SharedViewModel
        sharedViewModel.displayName.observe(viewLifecycleOwner) { name ->
            Log.d("DBG_PROFILE", "ViewModel displayName changed: $name")
            binding.tvProfileName.text = name ?: auth.currentUser?.displayName ?: "User Name"
        }

        sharedViewModel.photoUri.observe(viewLifecycleOwner) { uri ->
            Log.d("DBG_PROFILE", "ViewModel photoUri changed: $uri")
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivProfileImage)
        }
    }

    private fun listenForProfileUpdate() {
        // Backup listener kalau ViewModel belum keupdate
        parentFragmentManager.setFragmentResultListener(
            "profile_updated",
            viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString("name")
            val photoUrl = bundle.getString("photoUrl")
            Log.d("DBG_PROFILE", "Received fragment result: name=$name, photoUrl=$photoUrl")

            // Fragment result akan trigger update ViewModel juga di ProfileEditFragment
            // Jadi observer ViewModel di atas yang akan handle update UI
        }
    }

    override fun onResume() {
        super.onResume()
        // Hanya reload kalau data masih default
        if (binding.tvProfileName.text == "User Name") {
            loadUserData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}