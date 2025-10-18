package com.example.stratify

import StatusAdapter
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.FragmentWorkspaceDetailBinding

class WorkspaceDetailFragment : Fragment() {

    // View binding for the fragment's layout.
    private var _binding: FragmentWorkspaceDetailBinding? = null
    private val binding get() = _binding!!

    // Arguments passed from the previous fragment, including the workspace object.
    private val args: WorkspaceDetailFragmentArgs by navArgs()
    // ViewModel associated with this fragment for managing UI-related data.
    private val viewModel: WorkspaceDetailViewModel by viewModels()

    // Adapters for the status and progress RecyclerViews.
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var progressAdapter: ProgressAdapter

    /**
     * Inflates the layout for this fragment using view binding.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkspaceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called when the fragment's view has been created. Initializes UI components, observers, and listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- SET INITIAL DISPLAY --- //
        binding.tvWorkspaceTitle.text = args.workspace.name
        binding.tvWorkspaceId.text = args.workspace.id
        binding.tvPassword.text = args.workspace.password
        updateMemberCountUI()
        updateStatusUI(args.workspace.status)

        // --- ENABLE ALL FEATURES --- //
        setupInlineEditor()
        setupCopyListeners()
        setupStatusDropdown()
        setupProgressSection()

        // --- OBSERVERS --- //
        // Observe the progress list from the ViewModel and update the adapter.
        viewModel.progressList.observe(viewLifecycleOwner) {
            progressAdapter.submitList(it)
        }

        // --- BUTTON LISTENERS --- //
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.chipMembers.setOnClickListener {
            // Show the members dialog.
            val membersDialog = MembersDialogFragment.newInstance(args.workspace.members.toTypedArray())
            membersDialog.show(childFragmentManager, "MembersDialog")
        }
    }

    /**
     * Configures the RecyclerView for progress items and sets up listeners for adding new progress.
     */
    private fun setupProgressSection() {
        // Initialize the adapter with a callback for delete actions.
        progressAdapter = ProgressAdapter(emptyList()) { progressItem ->
            showDeleteConfirmationDialog(progressItem)
        }
        binding.rvProgressList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = progressAdapter
        }

        // Show the add progress card when the button is clicked.
        binding.btnAddProgress.setOnClickListener {
            binding.cardAddProgress.isVisible = true
            binding.btnAddProgress.isVisible = false
        }

        // Save the new progress when the save button is clicked.
        binding.btnSaveProgress.setOnClickListener {
            val progressText = binding.etProgressText.text.toString().trim()
            if (progressText.isNotEmpty()) {
                viewModel.addProgress(progressText)
                binding.etProgressText.text.clear()
                hideKeyboard(it)
                // Hide the input card and show the add button again.
                binding.cardAddProgress.isVisible = false
                binding.btnAddProgress.isVisible = true
            } else {
                Toast.makeText(context, "Progress text cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Displays a confirmation dialog before deleting a progress item.
     */
    private fun showDeleteConfirmationDialog(progressItem: ProgressItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Progress")
            .setMessage("Are you sure you want to delete this progress item?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.removeProgress(progressItem)
                Toast.makeText(context, "Progress deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Updates the member chip text based on the number of members.
     */
    private fun updateMemberCountUI() {
        val memberCount = args.workspace.members.size
        binding.chipMembers.text = when (memberCount) {
            0 -> "No member"
            1 -> "1 Member"
            else -> "$memberCount Members"
        }
    }

    /**
     * Sets up the RecyclerView that acts as a dropdown for workspace status.
     */
    private fun setupStatusDropdown() {
        val statusOptions = listOf("To Do", "In Progress", "To Verify", "Done")
        statusAdapter = StatusAdapter(statusOptions) { selectedStatus ->
            updateStatusUI(selectedStatus) // Update the button appearance.
            binding.rvStatusOptions.isVisible = false // Hide the dropdown.
            args.workspace.status = selectedStatus // Update the workspace object.
            sendResultBack() // Send the updated object back to the previous fragment.
        }

        binding.rvStatusOptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statusAdapter
        }

        // Toggle the visibility of the status dropdown.
        binding.btnStatus.setOnClickListener {
            binding.rvStatusOptions.isVisible = !binding.rvStatusOptions.isVisible
        }
    }

    /**
     * Updates the status button's text and background color based on the selected status.
     */
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

    /**
     * Sets up click listeners for copying the workspace ID and password.
     */
    private fun setupCopyListeners() {
        binding.btnCopyWorkspaceId.setOnClickListener {
            copyToClipboard("Workspace ID", binding.tvWorkspaceId.text.toString())
        }
        binding.btnCopyPassword.setOnClickListener {
            copyToClipboard("Password", binding.tvPassword.text.toString())
        }
    }

    /**
     * Copies the given text to the system clipboard and shows a confirmation toast.
     */
    private fun copyToClipboard(label: String, text: String) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    /**
     * Sends the updated workspace object back to the previous fragment using FragmentResultListener.
     */
    private fun sendResultBack() {
        parentFragmentManager.setFragmentResult("workspace_update_request", bundleOf("updated_workspace" to args.workspace))
    }

    // --- INLINE EDITING LOGIC --- //

    /**
     * Configures the inline editing functionality for the Department and Details fields.
     */
    private fun setupInlineEditor() {
        // Setup for Department field
        binding.tvDepartmentName.text = args.workspace.department.ifBlank { "Click here" }
        binding.tvDepartmentName.setOnClickListener {
            toggleDepartmentEdit(true)
        }
        binding.etDepartmentName.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newText = textView.text.toString()
                binding.tvDepartmentName.text = newText.ifBlank { "Click here" }
                args.workspace.department = newText
                sendResultBack()
                toggleDepartmentEdit(false)
                hideKeyboard(textView)
                return@setOnEditorActionListener true
            }
            false
        }
        binding.etDepartmentName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // Save when the EditText loses focus
                val newText = binding.etDepartmentName.text.toString()
                binding.tvDepartmentName.text = newText.ifBlank { "Click here" }
                args.workspace.department = newText
                sendResultBack()
                toggleDepartmentEdit(false)
            }
        }

        // Setup for Details field
        binding.tvDetailsContent.text = args.workspace.details.ifBlank { "Click here" }
        binding.tvDetailsContent.setOnClickListener {
            toggleDetailsEdit(true)
        }
        binding.btnSaveDetails.setOnClickListener {
            val newText = binding.etDetailsContent.text.toString()
            binding.tvDetailsContent.text = newText.ifBlank { "Click here" }
            args.workspace.details = newText
            sendResultBack()
            toggleDetailsEdit(false)
            hideKeyboard(it)
        }
    }

    /**
     * Toggles the UI between display mode (TextView) and edit mode (EditText) for the Department field.
     */
    private fun toggleDepartmentEdit(isEditing: Boolean) {
        binding.tvDepartmentName.isVisible = !isEditing
        binding.etDepartmentName.isVisible = isEditing
        if (isEditing) {
            val currentText = binding.tvDepartmentName.text.toString()
            binding.etDepartmentName.setText(if (currentText == "Click here") "" else currentText)
            binding.etDepartmentName.requestFocus()
            showKeyboard(binding.etDepartmentName)
        } else {
            hideKeyboard(binding.etDepartmentName)
        }
    }

    /**
     * Toggles the UI between display mode (TextView) and edit mode (EditText) for the Details field.
     */
    private fun toggleDetailsEdit(isEditing: Boolean) {
        binding.tvDetailsContent.isVisible = !isEditing
        binding.editDetailsGroup.isVisible = isEditing
        if (isEditing) {
            val currentText = binding.tvDetailsContent.text.toString()
            binding.etDetailsContent.setText(if (currentText == "Click here") "" else currentText)
            binding.etDetailsContent.requestFocus()
            showKeyboard(binding.etDetailsContent)
        } else {
            hideKeyboard(binding.etDetailsContent)
        }
    }

    // --- KEYBOARD HELPER FUNCTIONS --- //
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
        _binding = null
    }
}
