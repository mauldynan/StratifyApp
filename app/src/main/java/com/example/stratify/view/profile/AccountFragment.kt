package com.example.stratify.view.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.stratify.R
import com.example.stratify.databinding.FragmentAccountBinding
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logoutUser() {
        auth.signOut()
        // Navigate to the login screen, clearing the back stack
        findNavController().navigate(R.id.action_global_loginActivity)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}