package com.example.stratify.view.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.stratify.R
import com.example.stratify.databinding.FragmentAccountDeleteBinding
import com.google.firebase.auth.FirebaseAuth

class AccountDeleteFragment : DialogFragment() {

    private var _binding: FragmentAccountDeleteBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountDeleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.btnDeleteAccountFinal.setOnClickListener {
            deleteUserAccount()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun deleteUserAccount() {
        val user = auth.currentUser
        user?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    "Account deleted successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                // Navigate to the login screen, which should clear the back stack.
                // No need to call dismiss() before this, as it can cause a crash.
                findNavController().navigate(R.id.action_global_loginActivity)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to delete account. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}