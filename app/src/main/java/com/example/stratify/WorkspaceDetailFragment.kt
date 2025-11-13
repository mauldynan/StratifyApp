package com.example.stratify

import StatusAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.FragmentWorkspaceDetailBinding
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class WorkspaceDetailFragment : Fragment() {

    private var _binding: FragmentWorkspaceDetailBinding? = null
    private val binding get() = _binding!!

    private val args: WorkspaceDetailFragmentArgs by navArgs()
    private val repository = WorkspaceRepository()

    private lateinit var statusAdapter: StatusAdapter
    private lateinit var progressAdapter: ProgressAdapter

    private var workspaceListener: ListenerRegistration? = null
    private var progressListener: ListenerRegistration? = null
    private var currentWorkspace: Workspace? = null
    private val progressList = mutableListOf<ProgressItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkspaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Request notification permission
        requestNotificationPermission()
        WorkspaceNotificationService.start(requireContext(), args.workspace.id)
        setupWorkspaceListener()
        setupProgressListener()
        setupInlineEditor()
        setupCopyListeners()
        setupStatusDropdown()
        setupProgressSection()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.chipMembers.setOnClickListener {
            currentWorkspace?.let { workspace ->
                val membersDialog = MembersDialogFragment.newInstance(
                    workspace.members.toTypedArray(),     // 1. Array Nama
                    workspace.memberIds.toTypedArray(),   // 2. Array ID
                    HashMap(workspace.memberPhotos)       // 3. Peta Foto
                )
                membersDialog.show(childFragmentManager, "MembersDialog")
            }
        }

    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private fun setupWorkspaceListener() {
        val workspaceId = args.workspace.id

        workspaceListener = repository.db.collection("workspaces")
            .document(workspaceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                snapshot?.toObject(Workspace::class.java)?.let { workspace ->
                    currentWorkspace = workspace
                    updateUI(workspace)
                }
            }
    }

    // Setup real-time listener untuk progress
    private fun setupProgressListener() {
        val workspaceId = args.workspace.id

        progressListener = repository.listenToProgress(
            workspaceId = workspaceId,
            onProgressChanged = { progressItems ->
                progressList.clear()
                progressList.addAll(progressItems)
                progressAdapter.submitList(progressItems)
            },
            onError = { error ->
                Toast.makeText(
                    requireContext(),
                    "Progress error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun updateUI(workspace: Workspace) {
        binding.tvWorkspaceTitle.text = workspace.name
        binding.tvWorkspaceId.text = workspace.id
        binding.tvPassword.text = workspace.password

        val memberCount = workspace.members.size
        binding.chipMembers.text = when (memberCount) {
            0 -> "No member"
            1 -> "1 Member"
            else -> "$memberCount Members"
        }

        updateStatusUI(workspace.status)

        if (!binding.etDepartmentName.isVisible) {
            binding.tvDepartmentName.text = workspace.department.ifBlank { "Click here" }
        }
        if (!binding.editDetailsGroup.isVisible) {
            binding.tvDetailsContent.text = workspace.details.ifBlank { "Click here" }
        }
    }

    private fun setupProgressSection() {
        progressAdapter = ProgressAdapter(emptyList()) { progressItem ->
            showDeleteConfirmationDialog(progressItem)
        }
        binding.rvProgressList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = progressAdapter
        }

        binding.btnAddProgress.setOnClickListener {
            binding.cardAddProgress.isVisible = true
            binding.btnAddProgress.isVisible = false
        }

        binding.btnSaveProgress.setOnClickListener {
            val progressText = binding.etProgressText.text.toString().trim()
            if (progressText.isNotEmpty()) {
                addProgress(progressText)
            } else {
                Toast.makeText(context, "Progress text cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Add progress to Firestore
    @SuppressLint("SetTextI18n")
    private fun addProgress(progressText: String) {
        val workspaceId = args.workspace.id

        binding.btnSaveProgress.isEnabled = false
        binding.btnSaveProgress.text = "Saving..."

        lifecycleScope.launch {
            val result = repository.addProgress(workspaceId, progressText)

            result.onSuccess {
                binding.etProgressText.text.clear()
                hideKeyboard(binding.btnSaveProgress)
                binding.cardAddProgress.isVisible = false
                binding.btnAddProgress.isVisible = true

                Toast.makeText(
                    requireContext(),
                    "Progress added successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to add progress: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            binding.btnSaveProgress.isEnabled = true
            binding.btnSaveProgress.text = "Save"
        }
    }

    private fun showDeleteConfirmationDialog(progressItem: ProgressItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Progress")
            .setMessage("Are you sure you want to delete this progress item?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProgress(progressItem)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Delete progress from Firestore
    private fun deleteProgress(progressItem: ProgressItem) {
        val workspaceId = args.workspace.id

        lifecycleScope.launch {
            val result = repository.deleteProgress(workspaceId, progressItem.id)

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "Progress deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupStatusDropdown() {
        val statusOptions = listOf("To Do", "In Progress", "To Verify", "Done")
        statusAdapter = StatusAdapter(statusOptions) { selectedStatus ->
            updateStatusUI(selectedStatus)
            binding.rvStatusOptions.isVisible = false

            currentWorkspace?.let { workspace ->
                val updatedWorkspace = workspace.copy(status = selectedStatus)
                saveWorkspaceToFirestore(updatedWorkspace)
            }
        }

        binding.rvStatusOptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statusAdapter
        }

        binding.btnStatus.setOnClickListener {
            binding.rvStatusOptions.isVisible = !binding.rvStatusOptions.isVisible
        }
    }

    private fun updateStatusUI(status: String) {
        binding.tvStatusText.text = status
        val colorRes = when (status) {
            "In Progress" -> R.color.status_inprogress
            "To Verify" -> R.color.status_toverify
            "Done" -> R.color.status_done
            else -> R.color.status_todo
        }
        binding.btnStatus.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)
    }

    private fun setupCopyListeners() {
        binding.btnCopyWorkspaceId.setOnClickListener {
            copyToClipboard("Workspace ID", binding.tvWorkspaceId.text.toString())
        }
        binding.btnCopyPassword.setOnClickListener {
            copyToClipboard("Password", binding.tvPassword.text.toString())
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun saveWorkspaceToFirestore(workspace: Workspace) {
        lifecycleScope.launch {
            val result = repository.updateWorkspace(workspace)
            result.onFailure { error ->
                Toast.makeText(context, "Failed to update: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupInlineEditor() {
        binding.tvDepartmentName.setOnClickListener {
            currentWorkspace?.let {
                toggleDepartmentEdit(true, it.department)
            }
        }

        binding.etDepartmentName.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveDepartment(textView.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }

        binding.etDepartmentName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveDepartment(binding.etDepartmentName.text.toString())
            }
        }

        binding.tvDetailsContent.setOnClickListener {
            currentWorkspace?.let {
                toggleDetailsEdit(true, it.details)
            }
        }

        binding.btnSaveDetails.setOnClickListener {
            saveDetails(binding.etDetailsContent.text.toString())
        }
    }

    private fun saveDepartment(newText: String) {
        binding.tvDepartmentName.text = newText.ifBlank { "Click here" }
        currentWorkspace?.let { workspace ->
            val updated = workspace.copy(department = newText)
            saveWorkspaceToFirestore(updated)
        }
        toggleDepartmentEdit(false, "")
        hideKeyboard(binding.etDepartmentName)
    }

    private fun saveDetails(newText: String) {
        binding.tvDetailsContent.text = newText.ifBlank { "Click here" }
        currentWorkspace?.let { workspace ->
            val updated = workspace.copy(details = newText)
            saveWorkspaceToFirestore(updated)
        }
        toggleDetailsEdit(false, "")
        hideKeyboard(binding.etDetailsContent)
    }

    private fun toggleDepartmentEdit(isEditing: Boolean, currentText: String) {
        binding.tvDepartmentName.isVisible = !isEditing
        binding.etDepartmentName.isVisible = isEditing
        if (isEditing) {
            binding.etDepartmentName.setText(currentText.ifBlank { "" })
            binding.etDepartmentName.requestFocus()
            showKeyboard(binding.etDepartmentName)
        } else {
            hideKeyboard(binding.etDepartmentName)
        }
    }

    private fun toggleDetailsEdit(isEditing: Boolean, currentText: String) {
        binding.tvDetailsContent.isVisible = !isEditing
        binding.editDetailsGroup.isVisible = isEditing
        if (isEditing) {
            binding.etDetailsContent.setText(currentText.ifBlank { "" })
            binding.etDetailsContent.requestFocus()
            showKeyboard(binding.etDetailsContent)
        } else {
            hideKeyboard(binding.etDetailsContent)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        workspaceListener?.remove()
        progressListener?.remove()  // Remove progress listener

        // Stop service ketika keluar dari detail workspace
        WorkspaceNotificationService.stop(requireContext())
        _binding = null
    }
}