package com.example.stratify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.stratify.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    // View binding for the fragment's layout.
    private var _binding: FragmentStartBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    // Retrieves navigation arguments passed to this fragment.
    private val args: StartFragmentArgs by navArgs()

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Sets up the views and click listeners after the view has been created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Conditionally show the back button based on the navigation argument.
        if (args.showBackButton) {    // Use the CORRECT ID: btnBack
            binding.header.btnBack.isVisible = true
            binding.header.btnBack.setOnClickListener {
                findNavController().navigateUp() // Navigate back to the previous screen.
            }
        }

        // Set a click listener for the "Create Workspace" button.
        binding.btnCreate.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_createWorkspaceFragment)
        }

        // Set a click listener for the "Join Workspace" button.
        binding.btnJoin.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_joinWorkspaceFragment)
        }
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
