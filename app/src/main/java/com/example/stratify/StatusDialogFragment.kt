package com.example.stratify

import StatusAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.DialogStatusBinding

class StatusDialogFragment : DialogFragment() {

    private var _binding: DialogStatusBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedStatus: StatusOption

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogStatusBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusOptions = listOf(
            StatusOption("To Do", R.color.status_todo),
            StatusOption("In Progress", R.color.status_inprogress),
            StatusOption("To Verify", R.color.status_toverify),
            StatusOption("Done", R.color.status_done)
        )
        // Set initial status
        selectedStatus = statusOptions[0]
        updateSelectedStatusView()

        // Setup RecyclerView for dropdown
        val statusNames = statusOptions.map { it.name }
        val statusAdapter = StatusAdapter(statusNames) { option ->
            selectedStatus = statusOptions.first { it.name == option }
            updateSelectedStatusView()
            binding.rvStatusOptions.isVisible = false
        }

        binding.rvStatusOptions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = statusAdapter
        }

        // Show/hide dropdown when status "button" is clicked
        binding.btnCurrentStatus.setOnClickListener {
            binding.rvStatusOptions.isVisible = !binding.rvStatusOptions.isVisible
        }

        // Listener for the close (X) button
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Set a listener for the Done button
        binding.btnDoneStatus.setOnClickListener {
            setFragmentResult("status_request", bundleOf(
                "new_status_name" to selectedStatus.name,
                "new_status_color" to selectedStatus.colorResId,
                "new_note" to binding.etNote.text.toString()
            ))
            dismiss()
        }
    }

    private fun updateSelectedStatusView() {
        // Update the text in the inner TextView
        binding.tvStatusText.text = selectedStatus.name
        // Update background color in outer ConstraintLayout
        binding.btnCurrentStatus.backgroundTintList = ContextCompat.getColorStateList(requireContext(), selectedStatus.colorResId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}