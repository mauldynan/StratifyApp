package com.example.stratify.ui.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.WorkspaceTask
import com.example.stratify.view.profile.SharedViewModel
import java.util.UUID

private val MaroonPrimary = Color(0xFF8B0000)

@Composable
fun WorkspaceDetailScreen(
    viewModel: SharedViewModel,
    workspaceId: String?,
    onBackPressed: () -> Unit
) {
    // 1. Efficiently find the workspace whenever the list or ID changes
    // derivedStateOf prevents unnecessary recompositions if unrelated parts of the VM change
    val workspace by remember(viewModel.workspaces, workspaceId) {
        derivedStateOf {
            viewModel.workspaces.find { it.id == workspaceId }
        }
    }

    // 2. Determine UI State
    when {
        viewModel.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaroonPrimary)
            }
        }
        workspace != null -> {
            WorkspaceDetailContent(
                workspace = workspace!!, // Force unwrap is safe here
                onBackPressed = onBackPressed,
                onUpdateWorkspace = { updated -> viewModel.updateWorkspace(updated) },
                onDeleteWorkspace = {
                    viewModel.deleteWorkspace(workspace!!)
                    onBackPressed()
                }
            )
        }
        else -> {
            // 3. Handle "Not Found" explicitly to stop the spinner from spinning forever
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.heightIn(16.dp))
                    Text("Workspace not found", color = Color.Gray)
                    Button(
                        onClick = onBackPressed,
                        colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailContent(
    workspace: Workspace,
    onBackPressed: () -> Unit,
    onUpdateWorkspace: (Workspace) -> Unit,
    onDeleteWorkspace: () -> Unit
) {
    var showMembersDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- Dialogs ---

    if (showMembersDialog) {
        AlertDialog(
            onDismissRequest = { showMembersDialog = false },
            title = { Text("Active Members") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(workspace.members ?: emptyList()) { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(member, fontSize = 16.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMembersDialog = false }) {
                    Text("Close", color = MaroonPrimary)
                }
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskCreated = { newTask ->
                // Ensure a completely new list reference is created for Compose to react
                val currentTasks = workspace.tasks ?: emptyList()
                val newTaskList = ArrayList(currentTasks).apply { add(newTask) }
                onUpdateWorkspace(workspace.copy(tasks = newTaskList))
                showAddTaskDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Workspace") },
            text = { Text("Are you sure you want to delete this workspace? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWorkspace()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Main UI ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = workspace.name ?: "Untitled Workspace",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaroonPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Status Section
            EditableStatusSection(
                currentStatus = workspace.status ?: "To Do",
                onStatusChange = { newStatus ->
                    onUpdateWorkspace(workspace.copy(status = newStatus))
                }
            )

            // 2. Membership Status (Only if joined)
            if (workspace.isJoined == true) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Membership", tint = MaroonPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Membership", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        HorizontalDivider()
                        OutlinedTextField(
                            value = "Joined",
                            onValueChange = {},
                            label = { Text("Status") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaroonPrimary,
                                focusedLabelColor = MaroonPrimary
                            )
                        )
                    }
                }
            }

            // 3. Member Status
            MemberStatusCard(
                memberCount = workspace.members?.size ?: 0,
                onClick = { showMembersDialog = true }
            )

            // 4. Tasks Section
            TasksSection(
                tasks = workspace.tasks ?: emptyList(),
                onAddTask = { showAddTaskDialog = true },
                onTaskChecked = { task, isChecked ->
                    val currentTasks = workspace.tasks ?: emptyList()
                    val newTaskList = ArrayList<WorkspaceTask>()

                    currentTasks.forEach { t ->
                        if (t.id == task.id) {
                            newTaskList.add(t.copy(isCompleted = isChecked))
                        } else {
                            newTaskList.add(t)
                        }
                    }
                    onUpdateWorkspace(workspace.copy(tasks = newTaskList))
                }
            )

            // 5. Details
            EditableDetailsCard(
                department = workspace.department ?: "",
                details = workspace.details ?: "",
                onDepartmentChange = { onUpdateWorkspace(workspace.copy(department = it)) },
                onDetailsChange = { onUpdateWorkspace(workspace.copy(details = it)) }
            )

            // 6. Security
            SecurityCard(
                workspaceId = workspace.id,
                password = workspace.password ?: ""
            )
        }
    }
}

// --- Helper Components ---

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskCreated: (WorkspaceTask) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedButton(
                    onClick = { launcher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedUri != null) "File Selected" else "Attach Picture/File",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (selectedUri != null) {
                    Text(
                        text = "Attached: ${selectedUri!!.lastPathSegment ?: "File"}",
                        fontSize = 12.sp,
                        color = MaroonPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskName.isNotBlank()) {
                        val newTask = WorkspaceTask(
                            id = UUID.randomUUID().toString(),
                            title = taskName,
                            description = taskDescription,
                            attachmentUri = selectedUri?.toString()
                        )
                        onTaskCreated(newTask)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun TasksSection(
    tasks: List<WorkspaceTask>,
    onAddTask: () -> Unit,
    onTaskChecked: (WorkspaceTask, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = "Tasks", tint = MaroonPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Workspace Tasks", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onAddTask) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaroonPrimary)
                }
            }
            HorizontalDivider()

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks yet. Click '+' to add one.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { isChecked ->
                                    onTaskChecked(task, isChecked)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = MaroonPrimary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (task.isCompleted) Color.Gray else Color.Black
                                )
                                if (task.attachmentUri != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = "Attachment",
                                            modifier = Modifier.size(12.dp),
                                            tint = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Attachment", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun EditableStatusSection(currentStatus: String, onStatusChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("To Do", "In Progress", "To Verify", "Done")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Box {
            WorkspaceStatusBadge(status = currentStatus, onClick = { expanded = true })
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            onStatusChange(status)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkspaceStatusBadge(status: String, onClick: () -> Unit) {
    val (color, displayName) = when (status) {
        "To Do" -> Color.Gray to "To Do"
        "In Progress" -> Color.Blue to "In Progress"
        "To Verify" -> Color(0xFFFFA500) to "To Verify" // Orange
        "Done" -> Color(0xFF4CAF50) to "Done" // Green
        else -> Color.Gray to status
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(50),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayName,
                modifier = Modifier.padding(end = 4.dp),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Change Status", tint = color)
        }
    }
}

@Composable
fun MemberStatusCard(memberCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, contentDescription = "Members", tint = MaroonPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Active Members", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$memberCount",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaroonPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun EditableDetailsCard(
    department: String,
    details: String,
    onDepartmentChange: (String) -> Unit,
    onDetailsChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaroonPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Workspace Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            HorizontalDivider()

            OutlinedTextField(
                value = department,
                onValueChange = onDepartmentChange,
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    focusedLabelColor = MaroonPrimary
                )
            )

            OutlinedTextField(
                value = details,
                onValueChange = onDetailsChange,
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    focusedLabelColor = MaroonPrimary
                )
            )
        }
    }
}

@Composable
fun SecurityCard(workspaceId: String, password: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Security", tint = MaroonPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Access Credentials", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            HorizontalDivider()
            CopyableRow(label = "Workspace ID", value = workspaceId)
            CopyableRow(label = "Password", value = password)
        }
    }
}

@Composable
fun CopyableRow(label: String, value: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        IconButton(onClick = {
            copyToClipboard(context, label, value)
        }) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy $label", tint = MaroonPrimary)
        }
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
}