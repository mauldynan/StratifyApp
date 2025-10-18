package com.example.stratify

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.FragmentWorkspaceListBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkspaceListFragment : Fragment() {

    private var _binding: FragmentWorkspaceListBinding? = null
    private val binding get() = _binding!!

    private val workspaceList = mutableListOf<Workspace>()
    private lateinit var workspaceAdapter: WorkspaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkspaceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadWorkspaces()

        // Setup Adapter
        workspaceAdapter = WorkspaceAdapter(workspaceList, {
            // On item click
            val action = WorkspaceListFragmentDirections.actionWorkspaceListFragmentToWorkspaceDetailFragment(it)
            findNavController().navigate(action)
        }, {
            // On delete click
            showDeleteConfirmationDialog(it)
        })

        // Setup RecyclerView
        binding.rvWorkspaces.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = workspaceAdapter
        }

        // Listener to receive data from Create/Join and Detail fragments
        parentFragmentManager.setFragmentResultListener("workspace_update_request", viewLifecycleOwner) { _, bundle ->
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
                saveWorkspaces()
            }
        }

        binding.btnAddWorkspace.setOnClickListener {
            val action = WorkspaceListFragmentDirections.actionWorkspaceListFragmentToStartFragment(showBackButton = true)
            findNavController().navigate(action)
        }
    }

    private fun showDeleteConfirmationDialog(workspace: Workspace) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Workspace")
            .setMessage("Are you sure you want to delete this workspace?")
            .setPositiveButton("Yes") { _, _ ->
                val index = workspaceList.indexOf(workspace)
                if (index != -1) {
                    workspaceList.removeAt(index)
                    saveWorkspaces()
                    workspaceAdapter.notifyItemRemoved(index)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun saveWorkspaces() {
        val sharedPrefs = requireActivity().getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val gson = Gson()
        val json = gson.toJson(workspaceList)
        editor.putString("workspaces", json)
        editor.apply()
    }

    private fun loadWorkspaces() {
        val sharedPrefs = requireActivity().getSharedPreferences("workspace_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPrefs.getString("workspaces", null)
        val type = object : TypeToken<MutableList<Workspace>>() {}.type
        if (json != null) {
            val workspaces: MutableList<Workspace> = gson.fromJson(json, type)
            workspaceList.clear()
            workspaceList.addAll(workspaces)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}