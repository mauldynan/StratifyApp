package com.example.stratify.ui.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.view.profile.SharedViewModel

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
    val workspace by viewModel.getWorkspaceById(workspaceId ?: "").observeAsState()

    if (workspace != null) {
        WorkspaceDetailContent(
            workspace = workspace!!,
            onBackPressed = onBackPressed,
            onUpdateWorkspace = { updated -> viewModel.updateWorkspace(updated) },
            onDeleteWorkspace = {
                viewModel.deleteWorkspace(workspace!!)
                onBackPressed()
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = maroonPrimary)
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf(workspace.name) }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Workspace?", fontWeight = FontWeight.Bold) },
            text = { Text("Tindakan ini tidak dapat dibatalkan. Semua data di dalam workspace ini akan hilang.") },
            confirmButton = {
                TextButton(onClick = onDeleteWorkspace) {
                    Text("HAPUS", color = negativeRed, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("BATAL", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog Edit Nama
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Nama Workspace", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nama Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateWorkspace(workspace.copy(name = tempName))
                    showEditNameDialog = false
                }) {
                    Text("SIMPAN", color = maroonPrimary, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("BATAL", color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(workspace.name, fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                        IconButton(onClick = { showEditNameDialog = true }) {
                            Icon(Icons.Default.Edit, "Edit Name", tint = goldAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                },
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
            // 1. Status Section (Dropdown Selection)
            StatusSelectionSection(
                currentStatus = workspace.status,
                onStatusChange = { newStatus -> onUpdateWorkspace(workspace.copy(status = newStatus)) }
            )

            // 2. Access Credentials (ID & Password)
            AccessCredentialsCard(id = workspace.id, password = workspace.password)

            // 3. Workspace Detail Form (Department & Task)
            DetailFormCard(
                department = workspace.department,
                details = workspace.details,
                onUpdate = { dept, detail ->
                    onUpdateWorkspace(workspace.copy(department = dept, details = detail))
                }
            )

            // 4. Members Section
            MembersListCard(members = workspace.members)

            Spacer(modifier = Modifier.height(40.dp))
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
            StatusBadge(status = currentStatus, onClick = { expanded = true })
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White).width(180.dp)
            ) {
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
fun StatusBadge(status: String, onClick: () -> Unit) {
    val color = when (status) {
        "To Do" -> Color.Gray
        "In Progress" -> Color(0xFF1976D2)
        "To Verify" -> Color(0xFFF57C00)
        "Done" -> Color(0xFF388E3C)
        else -> maroonPrimary
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
fun AccessCredentialsCard(id: String, password: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = maroonPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("AKSES KREDENSIAL", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
            }

            CredentialRow(label = "Room ID", value = id, context = context)
            CredentialRow(label = "Password", value = password, context = context)
        }
    }
}

@Composable
fun CredentialRow(label: String, value: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(lightBg)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = maroonPrimary)
        }
        IconButton(onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
            Toast.makeText(context, "$label disalin!", Toast.LENGTH_SHORT).show()
        }) {
            Icon(Icons.Default.ContentCopy, null, tint = maroonPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun DetailFormCard(
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

@Composable
fun MembersListCard(members: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = maroonPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ANGGOTA JOINED", fontSize = 11.sp, fontWeight = FontWeight.Black, color = maroonPrimary)
                }
                Surface(color = goldAccent, shape = CircleShape) {
                    Text("${members.size}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(Modifier.height(16.dp))

            members.forEach { member ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(maroonPrimary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Text(member.take(1).uppercase(), color = maroonPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(member, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkspaceDetailScreenPreview() {
    WorkspaceDetailContent(
        workspace = Workspace(
            id = "992102",
            name = "Stratify Design Team",
            status = "In Progress",
            department = "UI/UX Dept",
            details = "Merancang antarmuka aplikasi Android untuk proyek Stratify v2.0.",
            password = "ADMIN-2025",
            members = arrayListOf("Andi Wijaya", "Siti Aminah", "Rizky Fauzi"),
            isJoined = true
        ),
        onBackPressed = {},
        onUpdateWorkspace = {},
        onDeleteWorkspace = {}
    )
}