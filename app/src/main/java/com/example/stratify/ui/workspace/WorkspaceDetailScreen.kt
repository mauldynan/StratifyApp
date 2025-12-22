package com.example.stratify.ui.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.WorkspaceTask
import com.example.stratify.ui.theme.MaroonPrimary
import com.example.stratify.view.profile.SharedViewModel
import java.util.UUID

// --- Design Tokens ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)
private val negativeRed = Color(0xFFEF4444)

@Composable
fun WorkspaceDetailScreen(
    viewModel: SharedViewModel,
    workspaceId: String?,
    onBackPressed: () -> Unit
) {
    // Find the workspace in the list to get the latest state
    val workspace = viewModel.workspaces.find { it.id == workspaceId }

    if (workspace != null) {
        WorkspaceDetailContent(
            workspace = workspace,
            onBackPressed = onBackPressed,
            onUpdateWorkspace = { updated -> viewModel.updateWorkspace(updated) },
            onDeleteWorkspace = {
                viewModel.deleteWorkspace(workspace!!)
                onBackPressed()
            }
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
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
    onUpdateWorkspace: (Workspace) -> Unit,
    onDeleteWorkspace: () -> Unit
) {
    var showMembersDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    // --- Dialog: Members List ---
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
                        HorizontalDivider()
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

    // --- Dialog: Add Task ---
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskCreated = { newTask ->
                // CRITICAL FIX: Create a completely NEW ArrayList.
                // If we reuse the old list reference, Compose won't detect the change.
                val currentTasks = workspace.tasks ?: arrayListOf()
                val newTaskList = ArrayList<WorkspaceTask>()
                newTaskList.addAll(currentTasks)
                newTaskList.add(newTask)

                // Update the workspace with the new list
                onUpdateWorkspace(workspace.copy(tasks = newTaskList))
                showAddTaskDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workspace.name ?: "Untitled Workspace", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = maroonPrimary)
            )
        },
        containerColor = lightBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Status Section
            EditableStatusSection(
                currentStatus = workspace.status,
                onStatusChange = { newStatus -> onUpdateWorkspace(workspace.copy(status = newStatus)) }
            )

            // 2. Membership Status (For current user)
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

            // 3. Member Status (Active Members Count)
            MemberStatusCard(
                memberCount = workspace.members.size,
                onClick = { showMembersDialog = true }
            )

            // 4. Tasks Section
            TasksSection(
                tasks = workspace.tasks ?: emptyList(),
                onAddTask = { showAddTaskDialog = true },
                onTaskChecked = { task, isChecked ->
                    // CRITICAL FIX: Rebuild list when checking items too
                    val currentList = workspace.tasks ?: arrayListOf()
                    val updatedList = ArrayList<WorkspaceTask>()

                    // Copy all items, modifying the one that was clicked
                    currentList.forEach { t ->
                        if (t.id == task.id) {
                            updatedList.add(t.copy(isCompleted = isChecked))
                        } else {
                            updatedList.add(t)
                        }
                    }
                    onUpdateWorkspace(workspace.copy(tasks = updatedList))
                }
            )

            // 5. Workspace Details
            EditableDetailsCard(
                department = workspace.department,
                details = workspace.details,
                onUpdate = { dept, detail ->
                    onUpdateWorkspace(workspace.copy(department = dept, details = detail))
                }
            )

            // 6. Security Card
            SecurityCard(
                workspaceId = workspace.id,
                password = workspace.password
            )
        }
    }
}

// --- SUB-COMPONENTS ---

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
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedUri != null) "File Selected" else "Attach Picture (Optional)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (selectedUri != null) {
                    Text(
                        text = "Attached: ${selectedUri!!.lastPathSegment}",
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks yet. Click '+' to add one.", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        TaskItem(task = task, onCheckedChange = { isChecked ->
                            onTaskChecked(task, isChecked)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(task: WorkspaceTask, onCheckedChange: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = MaroonPrimary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (task.isCompleted) Color.Gray else Color.Black
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (task.attachmentUri != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaroonPrimary)
                        Text("Attachment", fontSize = 10.sp, color = MaroonPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusSelectionSection(currentStatus: String, onStatusChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("To Do", "In Progress", "To Verify", "Done")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("STATUS WORKSPACE", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary, letterSpacing = 1.sp)
        Box {
            WorkspaceStatusBadge(status = currentStatus, onClick = { expanded = true })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status, fontWeight = FontWeight.Bold) },
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
        "To Verify" -> Color(0xFFFFA500) to "To Verify"
        "Done" -> Color(0xFF4CAF50) to "Done"
        else -> Color.Gray to status
    }

    Surface(
        onClick = onClick,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(12.dp))
            Text(status.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowDropDown, null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun EditableDetailsCard(
    department: String,
    details: String,
    onUpdate: (String, String) -> Unit
) {
    var deptState by remember { mutableStateOf(department) }
    var descState by remember { mutableStateOf(details) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = maroonPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("DETAIL TUGAS", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
            }
            HorizontalDivider()

            OutlinedTextField(
                value = deptState,
                onValueChange = {
                    deptState = it
                    onUpdate(it, descState)
                },
                label = { Text("Departemen Bertugas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Business, null, tint = maroonPrimary) }
            )

            OutlinedTextField(
                value = descState,
                onValueChange = {
                    descState = it
                    onUpdate(deptState, it)
                },
                label = { Text("Deskripsi Task") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Notes, null, tint = maroonPrimary) }
            )
        }
    }
}

// --- MISSING COMPONENTS ADDED HERE ---

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
                Column {
                    Text("Members", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("$memberCount Active Members", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = "View", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun MembersListCard(members: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = "Security", tint = MaroonPrimary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Security", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            HorizontalDivider()

            // Workspace ID
            OutlinedTextField(
                value = workspaceId,
                onValueChange = {},
                label = { Text("Workspace ID") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { CopyButton(workspaceId) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    focusedLabelColor = MaroonPrimary
                )
            )

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = {},
                label = { Text("Workspace Password") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { CopyButton(password) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaroonPrimary,
                    focusedLabelColor = MaroonPrimary
                )
            )
        }
    }
}

@Composable
fun CopyButton(textToCopy: String) {
    val context = LocalContext.current
    IconButton(onClick = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }) {
        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaroonPrimary)
    }
}

@Composable
fun HorizontalDivider() {
    Divider(
        color = Color.LightGray.copy(alpha = 0.5f),
        thickness = 1.dp
    )
}