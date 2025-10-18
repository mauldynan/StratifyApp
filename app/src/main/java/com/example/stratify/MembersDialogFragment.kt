package com.example.stratify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stratify.databinding.DialogMembersBinding

/**
 * A DialogFragment that displays a list of members in a workspace.
 */
class MembersDialogFragment : DialogFragment() {

    private var _binding: DialogMembersBinding? = null
    private val binding get() = _binding!!

    /**
     * Sets the size of the dialog when it starts.
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // Set the width to 90% of the screen width.
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    /**
     * Inflates the layout for this fragment and sets the dialog background to transparent.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMembersBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    /**
     * Sets up the views and listeners after the view has been created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set a click listener for the close button to dismiss the dialog.
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // Dummy member data.
        // TODO: Replace with actual data from the workspace.
        val dummyMembers = listOf(
            Member("Nama Anda", isYou = true),
            Member("Asep Galon"),
            Member("Siomay Intel")
        )

        // Set up the RecyclerView to display the list of members.
        val memberAdapter = MemberAdapter(dummyMembers)
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memberAdapter
        }
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MEMBERS = "members_list"
        /**
         * Creates a new instance of the MembersDialogFragment with the given list of members.
         */
        fun newInstance(members: Array<String>): MembersDialogFragment {
            val fragment = MembersDialogFragment()
            val args = Bundle().apply {
                putStringArray(ARG_MEMBERS, members)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
