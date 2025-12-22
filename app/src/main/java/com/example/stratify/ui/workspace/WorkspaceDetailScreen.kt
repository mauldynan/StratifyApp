package com.example.stratify.ui.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.ui.theme.MaroonPrimary
import com.example.stratify.view.profile.SharedViewModel

@Composable
fun WorkspaceDetailScreen(
    viewModel: SharedViewModel,
    workspaceId: String?,
    onBackPressed: () -> Unit
) {
    // FIX: Directly find the workspace in the list to ensure reactive updates.
    val workspace = viewModel.workspaces.find { it.id == workspaceId }

    if (workspace != null) {
        WorkspaceDetailContent(
            workspace = workspace,
            onBackPressed = onBackPressed,
            onUpdateWorkspace = { updatedWorkspace ->
                viewModel.updateWorkspace(updatedWorkspace)
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaroonPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceDetailContent(
    workspace: Workspace,
    onBackPressed: () -> Unit,
    onUpdateWorkspace: (Workspace) -> Unit
) {
    var showMembersDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Dialog: Members List
    if (showMembersDialog) {
        AlertDialog(
            onDismissRequest = { showMembersDialog = false },
            title = { Text("Active Members") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CRASH FIX: Handle null list AND null items inside the list
                    items(workspace.members ?: emptyList()) { member ->
                        if (member != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(member, fontSize = 16.sp)
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
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

    // Dialog: Add Task
    if (showAddTaskDialog) {
        AddTaskDialog(onDismiss = { showAddTaskDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                // CRASH FIX: Added Elvis operator (?:) to handle null name
                title = { Text(workspace.name ?: "Untitled Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaroonPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
            // 1. Status Section (Editable)
            // CRASH FIX: Default to "To Do" if status is null
            EditableStatusSection(
                currentStatus = workspace.status ?: "To Do",
                onStatusChange = { newStatus ->
                    onUpdateWorkspace(workspace.copy(status = newStatus))
                }
            )

            // 2. Membership Status (Conditional - Only for Joined Workspaces)
            if (workspace.isJoined) {
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

            // 3. Member Status (Clickable)
            // CRASH FIX: Safe check for null members list
            MemberStatusCard(
                memberCount = workspace.members?.size ?: 0,
                onClick = { showMembersDialog = true }
            )

            // 4. Tasks Section (New)
            TasksSection(onAddTask = { showAddTaskDialog = true })

            // 5. Workspace Details (Editable Department & Description)
            // CRASH FIX: Default to empty string if null
            EditableDetailsCard(
                department = workspace.department ?: "",
                details = workspace.details ?: "",
                onDepartmentChange = { newDept ->
                    onUpdateWorkspace(workspace.copy(department = newDept))
                },
                onDetailsChange = { newDetails ->
                    onUpdateWorkspace(workspace.copy(details = newDetails))
                }
            )

            // 6. Security (ID & Password)
            // CRASH FIX: Default to "Unknown" if null
            SecurityCard(
                workspaceId = workspace.id ?: "Unknown ID",
                password = workspace.password ?: "No Password"
            )
        }
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit) {
    var taskName by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf<String?>(null) }

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

                // File Attachment Button
                OutlinedButton(
                    onClick = {
                        // Mock file attachment
                        fileName = "screenshot_001.png"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = fileName ?: "Attach Picture/File")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Handle task creation here (needs backend integration)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaroonPrimary)
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun TasksSection(onAddTask: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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

            // Placeholder for list of tasks
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
        onClick = onClick // Make card clickable
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

@Preview(showBackground = true)
@Composable
fun WorkspaceDetailScreenPreview() {
    WorkspaceDetailContent(
        workspace = Workspace(
            name = "Project X",
            status = "In Progress",
            department = "Engineering",
            details = "Building the future of project management.",
            id = "WS-123456",
            password = "securePassword",
            members = arrayListOf("User1", "User2", "User3"),
            isJoined = true
        ),
        onBackPressed = {},
        onUpdateWorkspace = {}
    )
}