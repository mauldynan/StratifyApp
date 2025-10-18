package com.example.stratify

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.stratify.databinding.FragmentCreateWorkspaceBinding

class CreateWorkspaceFragment : Fragment() {

    private var _binding: FragmentCreateWorkspaceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up the views and listeners after the view has been created.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the back button to navigate up.
        binding.header.btnBack.setOnClickListener { findNavController().navigateUp() }

        // Generate and set the initial workspace ID.
        generateAndSetWorkspaceId()

        // Set the refresh icon on the workspace ID EditText.
        binding.etWorkspaceId.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_refresh, 0)
        // Add a touch listener to the workspace ID EditText to regenerate the ID when the refresh icon is clicked.
        binding.etWorkspaceId.setOnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                binding.etWorkspaceId.compoundDrawables[DRAWABLE_RIGHT]?.let { drawable ->
                    if (event.rawX >= (binding.etWorkspaceId.right - drawable.bounds.width())) {
                        generateAndSetWorkspaceId()
                        return@setOnTouchListener true
                    }
                }
            }
            return@setOnTouchListener false
        }

        // Set a click listener for the create workspace button.
        binding.btnCreateWorkspace.setOnClickListener {
            val workspaceId = binding.etWorkspaceId.text.toString()
            val workspaceName = binding.etName.text.toString()
            val password = binding.etPassword.text.toString()

            // Validate the workspace name.
            if (workspaceName.isBlank()) {
                binding.etName.error = "Nama Workspace tidak boleh kosong"
                return@setOnClickListener
            }

            // Validate the password.
            if (password.isBlank()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            // Create a new workspace object.
            val newWorkspace = Workspace(id = workspaceId, name = workspaceName, creatorName = "You", password = password, members = arrayListOf())
            // Show a success message.
            Toast.makeText(requireContext(), "Successfully created workspace!", Toast.LENGTH_SHORT).show()

            // Set the fragment result to pass the new workspace back to the previous fragment.
            setFragmentResult("workspace_update_request", bundleOf("updated_workspace" to newWorkspace))

            // Navigate to the workspace list fragment.
            findNavController().navigate(R.id.action_createWorkspaceFragment_to_workspaceListFragment)
        }
    }

    /**
     * Generates a random 6-character workspace ID and sets it to the EditText.
     */
    private fun generateAndSetWorkspaceId() {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val workspaceId = (1..6)
            .map { allowedChars.random() }
            .joinToString("")
        binding.etWorkspaceId.setText(workspaceId)
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}