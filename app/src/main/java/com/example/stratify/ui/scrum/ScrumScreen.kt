package com.example.stratify.ui.scrum

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.ui.theme.StratifyTheme

// --- Colors ---
private val maroonPrimary = Color(0xFF760000)
private val textYellow = Color(0xFFF6C761)

// --- Data Classes ---
enum class TaskStatus(val displayName: String) {
    TODO("To Do"),
    IN_PROGRESS("In Progress"),
    TO_VERIFY("To Verify"),
    DONE("Done")
}

data class Task(
    val id: Int,
    var name: String,
    var deadline: String,
    var department: String,
    var status: TaskStatus
)

fun getSampleTasks(): SnapshotStateList<Task> {
    return mutableStateListOf()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrumScreen() {
    val tasks = remember { getSampleTasks() }
    var searchQuery by remember { mutableStateOf("") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = if (searchQuery.isEmpty()) tasks else tasks.filter { it.name.contains(searchQuery, ignoreCase = true) }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { name, deadline, department ->
                val newId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                tasks.add(Task(newId, name, deadline, department, TaskStatus.TODO))
                showAddTaskDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scrum", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 20.sp, color = textYellow) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = maroonPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }, containerColor = maroonPrimary) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search ...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Task List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskItem(task = task, onUpdateTask = { updated ->
                        val index = tasks.indexOfFirst { it.id == updated.id }
                        if (index != -1) tasks[index] = updated
                    })
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onUpdateTask: (Task) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }

    var editedName by remember(task.name) { mutableStateOf(task.name) }
    var editedDeadline by remember(task.deadline) { mutableStateOf(task.deadline) }
    var editedDepartment by remember(task.department) { mutableStateOf(task.department) }
    var editedStatus by remember(task.status) { mutableStateOf(task.status) }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isEditing) {
                // Edit Mode
                OutlinedTextField(value = editedName, onValueChange = { editedName = it }, label = { Text("Task Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editedDeadline, onValueChange = { editedDeadline = it }, label = { Text("Deadline") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = editedDepartment, onValueChange = { editedDepartment = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())

                StatusSelector(selectedStatus = editedStatus, onStatusSelected = { editedStatus = it })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = {
                        onUpdateTask(task.copy(name = editedName, deadline = editedDeadline, department = editedDepartment, status = editedStatus))
                        isEditing = false
                    }) { Icon(Icons.Default.Done, contentDescription = "Save", tint = maroonPrimary) }

                    IconButton(onClick = { isEditing = false }) { Icon(Icons.Default.Close, contentDescription = "Cancel") }
                }
            } else {
                // Display Mode
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(task.name, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { isEditing = true }) { Icon(Icons.Default.Edit, contentDescription = "Edit Task") }
                }
                InfoRow("Deadline:", task.deadline)
                InfoRow("Department:", task.department)
                StatusBadge(task.status)
            }
        }
    }
}

@Composable
private fun AddTaskDialog(onDismiss: () -> Unit, onTaskAdded: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    val isFormValid by remember(name, deadline, department) { derivedStateOf { name.isNotBlank() && deadline.isNotBlank() && department.isNotBlank() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Task Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Deadline") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = { Button(onClick = { onTaskAdded(name, deadline, department) }, enabled = isFormValid) { Text("Add Task") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StatusSelector(selectedStatus: TaskStatus, onStatusSelected: (TaskStatus) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selectedStatus.displayName) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth()) {
            TaskStatus.values().forEach { status ->
                DropdownMenuItem(text = { Text(status.displayName) }, onClick = { onStatusSelected(status); expanded = false })
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val color = when (status) {
        TaskStatus.TODO -> Color.Gray
        TaskStatus.IN_PROGRESS -> Color.Blue
        TaskStatus.TO_VERIFY -> Color.Red
        TaskStatus.DONE -> maroonPrimary
    }
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Text(status.displayName, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = color, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(label, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, modifier = Modifier.width(100.dp))
        Text(value)
    }
}

@Preview(showBackground = true)
@Composable
fun ScrumScreenPreview() {
    StratifyTheme {
        ScrumScreen()
    }
}
