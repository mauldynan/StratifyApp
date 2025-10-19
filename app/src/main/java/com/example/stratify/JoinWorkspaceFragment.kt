package com.example.stratify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.stratify.databinding.FragmentJoinWorkspaceBinding
import kotlinx.coroutines.launch

class JoinWorkspaceFragment : Fragment() {

    private var _binding: FragmentJoinWorkspaceBinding? = null
    private val binding get() = _binding!!
    private val repository = WorkspaceRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJoinWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.header.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnJoinWorkspace.setOnClickListener {
            val workspaceId = binding.etWorkspaceId.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (workspaceId.isBlank()) {
                binding.etWorkspaceId.error = "ID tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isBlank()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            joinWorkspace(workspaceId, password)
        }
    }

    private fun joinWorkspace(workspaceId: String, password: String) {
        binding.btnJoinWorkspace.isEnabled = false
        binding.btnJoinWorkspace.text = "Joining..."

        lifecycleScope.launch {
            val result = repository.joinWorkspace(workspaceId, password)

            result.onSuccess { workspace ->
                Toast.makeText(
                    requireContext(),
                    "Successfully joined ${workspace.name}!",
                    Toast.LENGTH_SHORT
                ).show()

                setFragmentResult(
                    "workspace_update_request",
                    bundleOf("updated_workspace" to workspace)
                )

                findNavController().navigate(R.id.action_joinWorkspaceFragment_to_workspaceListFragment)
            }.onFailure { error ->
                when (error.message) {
                    "Workspace tidak ditemukan" -> {
                        binding.etWorkspaceId.error = "Workspace tidak ditemukan"
                        Toast.makeText(
                            requireContext(),
                            "Workspace dengan ID tersebut tidak ditemukan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    "Password salah" -> {
                        binding.etPassword.error = "Password salah"
                        Toast.makeText(
                            requireContext(),
                            "Password salah!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    "You are already a member" -> {
                        Toast.makeText(
                            requireContext(),
                            "You are already a member of this workspace",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_joinWorkspaceFragment_to_workspaceListFragment)
                    }
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                binding.btnJoinWorkspace.isEnabled = true
                binding.btnJoinWorkspace.text = "Join Workspace"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}