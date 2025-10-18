package com.example.scrum_section

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scrum_section.adapter.TaskAdapter
import com.example.scrum_section.data.TaskRepository
import com.example.scrum_section.model.Task
import com.example.scrum_section.util.TaskStatus
import com.example.stratify.R
import com.example.stratify.databinding.FragmentScrumBinding
import com.google.android.material.tabs.TabLayout

class ScrumFragment : Fragment() {

    private var _binding: FragmentScrumBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskAdapter
    private var currentStatus = TaskStatus.ALL
    private var fullTaskList = listOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScrumBinding.inflate(inflater, container, false)
        val view = binding.root

        setupRecyclerView()
        setupFab()
        setupTabLayout()
        setupSearch()

        return view
    }

    /**
     * Sets up the RecyclerView with its adapter and layout manager.
     */
    private fun setupRecyclerView() {
        fullTaskList = TaskRepository.getTasksByStatus(currentStatus)
        adapter = TaskAdapter(fullTaskList.toMutableList())
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    /**
     * Sets up the Floating Action Button to open the AddTaskDialog.
     */
    private fun setupFab() {
        binding.btnAddTask.setOnClickListener {
            // Show the dialog and pass a lambda to refresh data upon task creation.
            AddTaskDialog { refreshData() }.show(parentFragmentManager, "AddTaskDialog")
        }
    }

    /**
     * Configures the TabLayout for filtering tasks by their status.
     */
    private fun setupTabLayout() {
        val tabLayout = binding.tabLayout
        val tabData = listOf(
            "All" to R.color.gray,
            "To Do" to R.color.blue,
            "In Progress" to R.color.orange,
            "To Verify" to R.color.purple,
            "Done" to R.color.green
        )

        // Create and add tabs dynamically.
        tabData.forEach { (title, colorRes) ->
            val tab = tabLayout.newTab().setText(title)
            tab.view.setBackgroundColor(requireContext().getColor(colorRes))
            tabLayout.addTab(tab)
        }

        // Customize the selected tab indicator.
        tabLayout.setSelectedTabIndicatorColor(requireContext().getColor(R.color.redw))
        tabLayout.setSelectedTabIndicatorHeight(6)
        tabLayout.setSelectedTabIndicatorGravity(TabLayout.INDICATOR_GRAVITY_BOTTOM)

        // Add a listener to handle tab selection events.
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentStatus = when (tab?.position) {
                    1 -> TaskStatus.TODO
                    2 -> TaskStatus.IN_PROGRESS
                    3 -> TaskStatus.TO_VERIFY
                    4 -> TaskStatus.DONE
                    else -> TaskStatus.ALL
                }
                refreshData()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * Sets up the SearchView for filtering the task list.
     */
    private fun setupSearch() {
        binding.header.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Called when the user submits the search query.
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterAndSort(query)
                return true
            }

            // Called when the text in the search view changes.
            override fun onQueryTextChange(newText: String?): Boolean {
                filterAndSort(newText)
                return true
            }
        })

        try {
            customizeSearchView()
        } catch (e: Exception) {
            // Log or handle the exception if something goes wrong during customization.
        }
    }

    /**
     * Customizes the appearance of the SearchView, such as icon size and text style.
     */
    private fun customizeSearchView() {
        val searchTextId = binding.header.searchView.context.resources
            .getIdentifier("android:id/search_src_text", null, null)
        val searchText = binding.header.searchView.findViewById<TextView>(searchTextId)

        val searchIconId = binding.header.searchView.context.resources
            .getIdentifier("android:id/search_mag_icon", null, null)
        val searchIcon = binding.header.searchView.findViewById<ImageView>(searchIconId)

        // ... (rest of the code is the same)

        val layoutParams = searchIcon.layoutParams
        layoutParams.width = (18 * resources.displayMetrics.density).toInt()
        layoutParams.height = (18 * resources.displayMetrics.density).toInt()
        searchIcon.layoutParams = layoutParams
    }

    /**
     * Reloads the task list from the repository and applies the current search filter.
     */
    private fun refreshData() {
        fullTaskList = TaskRepository.getTasksByStatus(currentStatus)
        // Apply the current search query to the newly loaded list.
        filterAndSort(binding.header.searchView.query.toString())
    }

    /**
     * Filters the task list based on the search query and updates the RecyclerView.
     * @param query The search text entered by the user.
     */
    private fun filterAndSort(query: String?) {
        val searchText = query?.lowercase()?.trim() ?: ""
        // Filter the full list based on the task name.
        val filtered = fullTaskList.filter { it.name.lowercase().contains(searchText) }
        adapter.updateData(filtered) // Update the adapter with the filtered data.
    }

    /**
     * Cleans up the binding when the view is destroyed to prevent memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
