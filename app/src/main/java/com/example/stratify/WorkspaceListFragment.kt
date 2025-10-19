package com.example.stratify

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.FragmentWorkspaceListBinding
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class WorkspaceListFragment : Fragment() {

    private var _binding: FragmentWorkspaceListBinding? = null
    private val binding get() = _binding!!

    private val repository = WorkspaceRepository()
    private val workspaceList = mutableListOf<Workspace>()
    private lateinit var workspaceAdapter: WorkspaceAdapter
    private var workspaceListener: ListenerRegistration? = null

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkspaceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupRealTimeListener()

        sharedViewModel.photoUri.observe(viewLifecycleOwner) {
            workspaceAdapter.refreshProfilePhotos()
        }

        sharedViewModel.displayName.observe(viewLifecycleOwner) {
            workspaceAdapter.refreshProfilePhotos()
        }

        parentFragmentManager.setFragmentResultListener(
            "workspace_update_request",
            viewLifecycleOwner
        ) { _, bundle ->
            val updatedWorkspace = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable("updated_workspace", Workspace::class.java)
            } else {
                @Suppress("DEPRECATION")
                bundle.getParcelable("updated_workspace")
            }

            updatedWorkspace?.let { ws ->
                val index = workspaceList.indexOfFirst { it.id == ws.id }
                if (index != -1) {
                    workspaceList[index] = ws
                    workspaceAdapter.notifyItemChanged(index)
                } else {
                    workspaceList.add(ws)
                    workspaceAdapter.notifyItemInserted(workspaceList.size - 1)
                }
            }
        }

        binding.btnAddWorkspace.setOnClickListener {
            val action = WorkspaceListFragmentDirections.actionWorkspaceListFragmentToStartFragment(
                showBackButton = true
            )
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        workspaceAdapter = WorkspaceAdapter(
            items = workspaceList,
            onItemClick = { workspace ->
                val action = WorkspaceListFragmentDirections
                    .actionWorkspaceListFragmentToWorkspaceDetailFragment(workspace)
                findNavController().navigate(action)
            },
            onDeleteClick = { workspace ->
                showDeleteConfirmationDialog(workspace)
            }
        )

        binding.rvWorkspaces.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workspaceAdapter
        }
    }

    private fun setupRealTimeListener() {
        workspaceListener = repository.listenToUserWorkspaces(
            onWorkspacesChanged = { workspaces ->
                workspaceList.clear()
                workspaceList.addAll(workspaces)
                workspaceAdapter.notifyDataSetChanged()
            },
            onError = { error ->
                Toast.makeText(
                    requireContext(),
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun showDeleteConfirmationDialog(workspace: Workspace) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Workspace")
            .setMessage("Are you sure you want to delete this workspace?")
            .setPositiveButton("Yes") { _, _ ->
                deleteWorkspace(workspace)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteWorkspace(workspace: Workspace) {
        lifecycleScope.launch {
            val result = repository.deleteWorkspace(workspace.id)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Workspace deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        workspaceListener?.remove()
        _binding = null
    }
}