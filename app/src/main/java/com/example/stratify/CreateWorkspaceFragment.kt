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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stratify.databinding.FragmentCreateWorkspaceBinding
import kotlinx.coroutines.launch

class CreateWorkspaceFragment : Fragment() {

    private var _binding: FragmentCreateWorkspaceBinding? = null
    private val binding get() = _binding!!
    private val repository = WorkspaceRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        generateAndSetWorkspaceId()

        binding.etWorkspaceId.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_refresh, 0)

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

        binding.btnCreateWorkspace.setOnClickListener {
            val workspaceId = binding.etWorkspaceId.text.toString()
            val workspaceName = binding.etName.text.toString()
            val password = binding.etPassword.text.toString()

            if (workspaceName.isBlank()) {
                binding.etName.error = "Nama Workspace tidak boleh kosong"
                return@setOnClickListener
            }

            if (password.isBlank()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            if (password.length < 4) {
                binding.etPassword.error = "Password minimal 4 karakter"
                return@setOnClickListener
            }

            createWorkspace(workspaceId, workspaceName, password)
        }
    }

    private fun createWorkspace(workspaceId: String, workspaceName: String, password: String) {
        binding.btnCreateWorkspace.isEnabled = false
        binding.btnCreateWorkspace.text = "Creating..."

        lifecycleScope.launch {
            val result = repository.createWorkspace(workspaceId, workspaceName, password)

            result.onSuccess { workspace ->
                Toast.makeText(
                    requireContext(),
                    "Successfully created workspace!",
                    Toast.LENGTH_SHORT
                ).show()

                setFragmentResult(
                    "workspace_update_request",
                    bundleOf("updated_workspace" to workspace)
                )

                findNavController().navigate(R.id.action_createWorkspaceFragment_to_workspaceListFragment)
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()

                binding.btnCreateWorkspace.isEnabled = true
                binding.btnCreateWorkspace.text = "Create Workspace"
            }
        }
    }

    private fun generateAndSetWorkspaceId() {
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val workspaceId = (1..6)
            .map { allowedChars.random() }
            .joinToString("")
        binding.etWorkspaceId.setText(workspaceId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}