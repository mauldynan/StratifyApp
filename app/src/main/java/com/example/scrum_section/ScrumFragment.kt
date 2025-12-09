package com.example.scrum_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrum_section.data.TaskRepository
import com.example.scrum_section.model.Task
import com.example.scrum_section.util.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- ViewModel ---
class ScrumViewModel : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedStatus = MutableStateFlow(TaskStatus.ALL)
    val selectedStatus: StateFlow<TaskStatus> = _selectedStatus

    val filteredTasks: StateFlow<List<Task>> = combine(
        _tasks, _searchQuery, _selectedStatus
    ) { tasks, query, status ->
        tasks.filter { task ->
            val statusMatch = if (status == TaskStatus.ALL) true else task.status == status
            val queryMatch = if (query.isBlank()) true else task.name.contains(query, ignoreCase = true)
            statusMatch && queryMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = TaskRepository.getTasks()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusSelected(status: TaskStatus) {
        _selectedStatus.value = status
    }
}

// --- Fragment ---
class ScrumFragment : Fragment() {

    private val viewModel: ScrumViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ScrumScreen(
                        viewModel = viewModel,
                        onTaskClicked = { task ->
                            TaskDetailDialog(task).show(childFragmentManager, "TaskDetailDialog")
                        },
                        onAddTaskClicked = {
                            AddTaskDialog {
                                // This lambda is called when a task is successfully added.
                                // We reload the tasks to refresh the list.
                                viewModel.loadTasks()
                            }.show(childFragmentManager, "AddTaskDialog")
                        }
                    )
                }
            }
        }
    }
}

// --- Composables ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrumScreen(
    viewModel: ScrumViewModel,
    onTaskClicked: (Task) -> Unit,
    onAddTaskClicked: () -> Unit
) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Search tasks...") },
                    singleLine = true
                )

                val statuses = TaskStatus.values()
                TabRow(selectedTabIndex = statuses.indexOf(selectedStatus)) {
                    statuses.forEach { status ->
                        Tab(
                            selected = status == selectedStatus,
                            onClick = { viewModel.onStatusSelected(status) },
                            text = { Text(status.name) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTaskClicked) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(tasks, key = { it.id }) { task ->
                TaskItem(task = task, onClick = { onTaskClicked(task) })
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Created by: ${task.createdBy}")
            Text(text = "Deadline: ${task.deadline}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.status.name,
                color = when (task.status) {
                    TaskStatus.TODO -> Color.Blue
                    TaskStatus.IN_PROGRESS -> Color(0xFFFFA500) // Orange
                    TaskStatus.TO_VERIFY -> Color.Magenta
                    TaskStatus.DONE -> Color.Green
                    else -> Color.Gray
                }
            )
        }
    }
}