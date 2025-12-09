package com.example.stratify.ui.scrum

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.R

// 1. Updated data classes to match your requirements
enum class TaskStatus(val displayName: String) {
    TODO("To-Do"),
    IN_PROGRESS("In Progress"),
    TO_VERIFY("To verify"),
    DONE("Done")
}

data class Task(
    val id: Int,
    var name: String,
    var deadline: String,
    var department: String,
    var status: TaskStatus
)

// --- Colors ---
private val maroonPrimary = Color(0xFF800000)
private val textYellow = Color(0xFFFFEB3B)

// 2. Updated sample data
fun getSampleTasks(): SnapshotStateList<Task> {
    return mutableStateListOf(
        Task(1, "Design new application logo", "2024-12-01", "UI/UX Design", TaskStatus.IN_PROGRESS),
        Task(2, "Develop REST API for user profiles", "2024-11-20", "Backend Development", TaskStatus.TODO),
        Task(3, "Fix authentication bug on Android", "2024-11-15", "Mobile App Team", TaskStatus.DONE)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrumScreen() {
    // State for the tasks, search query, and the add task dialog
    val tasks = remember { getSampleTasks() }
    var searchQuery by remember { mutableStateOf("") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = if (searchQuery.isEmpty()) {
        tasks
    } else {
        tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // --- Add Task Dialog ---
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { name, deadline, department ->
                val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                tasks.add(
                    Task(
                        id = newId,
                        name = name,
                        deadline = deadline,
                        department = department,
                        status = TaskStatus.TODO // New tasks default to "To-Do"
                    )
                )
                showAddTaskDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Scrum Board",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textYellow
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = maroonPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true }, // Updated to show the dialog
                containerColor = maroonPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Task", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 3. Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search tasks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. List of tasks
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items = filteredTasks, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onUpdateTask = { updatedTask ->
                            val index = tasks.indexOfFirst { it.id == updatedTask.id }
                            if (index != -1) {
                                tasks[index] = updatedTask
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onUpdateTask: (Task) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    // State holders for the text fields in edit mode
    var editedName by remember(task.name) { mutableStateOf(task.name) }
    var editedDeadline by remember(task.deadline) { mutableStateOf(task.deadline) }
    var editedDepartment by remember(task.department) { mutableStateOf(task.department) }
    var editedStatus by remember(task.status) { mutableStateOf(task.status) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isEditing) {
                // == EDIT MODE ==
                EditTaskView(
                    name = editedName,
                    deadline = editedDeadline,
                    department = editedDepartment,
                    status = editedStatus,
                    onNameChange = { editedName = it },
                    onDeadlineChange = { editedDeadline = it },
                    onDepartmentChange = { editedDepartment = it },
                    onStatusChange = { editedStatus = it },
                    onSave = {
                        val updatedTask = task.copy(
                            name = editedName,
                            deadline = editedDeadline,
                            department = editedDepartment,
                            status = editedStatus
                        )
                        onUpdateTask(updatedTask)
                        isEditing = false
                    },
                    onCancel = { isEditing = false }
                )
            } else {
                // == DISPLAY MODE ==
                DisplayTaskView(
                    task = task,
                    onEdit = { isEditing = true }
                )
            }
        }
    }
}

@Composable
private fun DisplayTaskView(task: Task, onEdit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = task.name, style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Task")
        }
    }
    InfoRow("Deadline:", task.deadline)
    InfoRow("Department:", task.department)
    StatusBadge(status = task.status)
}

@Composable
private fun EditTaskView(
    name: String,
    deadline: String,
    department: String,
    status: TaskStatus,
    onNameChange: (String) -> Unit,
    onDeadlineChange: (String) -> Unit,
    onDepartmentChange: (String) -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Task Name") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = deadline,
        onValueChange = onDeadlineChange,
        label = { Text("Deadline") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = department,
        onValueChange = onDepartmentChange,
        label = { Text("Department") },
        modifier = Modifier.fillMaxWidth()
    )

    StatusSelector(selectedStatus = status, onStatusSelected = onStatusChange)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSave) {
            Icon(Icons.Default.Done, contentDescription = "Save Changes", tint = maroonPrimary)
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Default.Close, contentDescription = "Cancel Edit")
        }
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (name: String, deadline: String, department: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    val isFormValid by remember(name, deadline, department) {
        derivedStateOf { name.isNotBlank() && deadline.isNotBlank() && department.isNotBlank() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline") },
                    placeholder = { Text("e.g., 2024-12-31") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onTaskAdded(name, deadline, department) },
                enabled = isFormValid
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StatusSelector(selectedStatus: TaskStatus, onStatusSelected: (TaskStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedStatus.displayName)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            TaskStatus.values().forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.displayName) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val color = when (status) {
        TaskStatus.TODO -> MaterialTheme.colorScheme.secondary
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        TaskStatus.TO_VERIFY -> MaterialTheme.colorScheme.error
        TaskStatus.DONE -> maroonPrimary
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(text = label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
        Text(text = value)
    }
}
