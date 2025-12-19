package com.example.stratify.ui.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
    val workspace by viewModel.getWorkspaceById(workspaceId ?: "").observeAsState()

    if (workspace != null) {
        WorkspaceDetailContent(
            workspace = workspace!!,
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workspace.name, fontWeight = FontWeight.Bold) },
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
            EditableStatusSection(
                currentStatus = workspace.status,
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

            // 3. Member Status
            MemberStatusCard(memberCount = workspace.members.size)

            // 4. Workspace Details (Editable Department & Description)
            EditableDetailsCard(
                department = workspace.department,
                details = workspace.details,
                onDepartmentChange = { newDept ->
                    onUpdateWorkspace(workspace.copy(department = newDept))
                },
                onDetailsChange = { newDetails ->
                    onUpdateWorkspace(workspace.copy(details = newDetails))
                }
            )

            // 5. Security (ID & Password)
            SecurityCard(workspaceId = workspace.id, password = workspace.password)
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
fun MemberStatusCard(memberCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
            Text(
                text = "$memberCount",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaroonPrimary
            )
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
