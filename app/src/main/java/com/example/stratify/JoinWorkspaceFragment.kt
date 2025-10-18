package com.example.stratify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.stratify.databinding.FragmentJoinWorkspaceBinding

class JoinWorkspaceFragment : Fragment() {

    private var _binding: FragmentJoinWorkspaceBinding? = null
    private val binding get() = _binding!!

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
            val workspaceId = binding.etWorkspaceId.text.toString()
            val password = binding.etPassword.text.toString()

            if (workspaceId.isBlank()) {
                binding.etWorkspaceId.error = "ID tidak boleh kosong"
                return@setOnClickListener
            }
            if (password.isBlank()) {
                binding.etPassword.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            // Di aplikasi nyata, Anda akan memverifikasi ID & password ke server dulu
            val joinedWorkspace = Workspace(
                id = workspaceId,
                name = "Joined Project ${workspaceId.take(4)}",
                creatorName = "Partner",
                password = password,
                status = "To Do",
                department = "",
                members = arrayListOf("You", "Partner", "Another Member"),
                details = ""
            )

            Toast.makeText(requireContext(), "Successfully joined workspace!", Toast.LENGTH_SHORT).show()

            setFragmentResult("workspace_update_request", bundleOf("updated_workspace" to joinedWorkspace))
            findNavController().navigate(R.id.action_joinWorkspaceFragment_to_workspaceListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}