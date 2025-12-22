package com.example.stratify.ui.workspace

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.view.profile.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Design Tokens (Premium Maroon & Gold) ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)
private val negativeRed = Color(0xFFEF4444)
private val cardOutline = Color(0xFFE2E8F0)

@Composable
fun WorkspaceDetailScreen(
    viewModel: SharedViewModel,
    workspaceId: String?,
    onBackPressed: () -> Unit
) {
    // Observasi data langsung dari ViewModel (Source of Truth)
    val workspace by viewModel.getWorkspaceById(workspaceId ?: "").observeAsState()

    if (workspace != null) {
        WorkspaceDetailContent(
            workspace = workspace!!,
            onBackPressed = onBackPressed,
            onUpdateWorkspace = { updated -> viewModel.updateWorkspace(updated) },
            onDeleteWorkspace = {
                viewModel.deleteWorkspace(workspace!!)
                onBackPressed()
            },
            onAddProgress = { text -> viewModel.addProgress(workspace!!.id, text) },
            onUpdateProgress = { detail -> viewModel.updateProgress(workspace!!.id, detail) },
            onDeleteProgress = { id -> viewModel.deleteProgress(workspace!!.id, id) }
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
    onDeleteWorkspace: () -> Unit,
    onAddProgress: (String) -> Unit,
    onUpdateProgress: (ProgressDetail) -> Unit,
    onDeleteProgress: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember(workspace.name) { mutableStateOf(workspace.name) }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Workspace?", fontWeight = FontWeight.Bold) },
            text = { Text("Tindakan ini tidak dapat dibatalkan. Semua data di dalam workspace ini akan hilang secara permanen.") },
            confirmButton = {
                Button(
                    onClick = onDeleteWorkspace,
                    colors = ButtonDefaults.buttonColors(containerColor = negativeRed),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("HAPUS", fontWeight = FontWeight.Bold, color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("BATAL", color = Color.Gray) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Dialog Edit Nama
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Nama Workspace", fontWeight = FontWeight.Black, color = maroonPrimary) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Nama Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = goldAccent)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateWorkspace(workspace.copy(name = tempName))
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("SIMPAN", fontWeight = FontWeight.Bold, color = goldAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("BATAL", color = Color.Gray) }
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
                        Text(
                            workspace.name,
                            fontWeight = FontWeight.Black,
                            color = goldAccent,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = goldAccent)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1, 2, & 4. Combined Overview Card (Status, Credentials, & Members)
            CombinedWorkspaceInfoCard(
                workspace = workspace,
                onStatusChange = { newStatus -> onUpdateWorkspace(workspace.copy(status = newStatus)) }
            )

            // 3. Detail Form (Form vs Summary)
            DetailFormCard(
                department = workspace.department,
                details = workspace.details,
                onUpdate = { dept, detail ->
                    onUpdateWorkspace(workspace.copy(department = dept, details = detail))
                }
            )

            // 5. Progress Section
            ProgressDetailCard(
                progressList = workspace.progressDetails,
                onAdd = onAddProgress,
                onEdit = onUpdateProgress,
                onDelete = onDeleteProgress
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = maroonPrimary.copy(alpha = 0.7f))
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = maroonPrimary.copy(alpha = 0.7f),
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
fun CombinedWorkspaceInfoCard(
    workspace: Workspace,
    onStatusChange: (String) -> Unit
) {
    val context = LocalContext.current
    var isMembersExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, cardOutline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row Utama: Status & Members Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    status = workspace.status,
                    onStatusChange = onStatusChange
                )

                // Anggota Summary (Clickable)
                Surface(
                    onClick = { isMembersExpanded = !isMembersExpanded },
                    color = maroonPrimary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, null, tint = maroonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${workspace.members.size} Anggota",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = maroonPrimary
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            if (isMembersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null,
                            tint = maroonPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Detail Anggota Tergabung (Expandable)
            AnimatedVisibility(
                visible = isMembersExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(lightBg)
                        .padding(12.dp)
                ) {
                    Text("DAFTAR ANGGOTA", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    workspace.members.forEach { member ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape).background(maroonPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(member.take(1).uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = maroonPrimary)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(member, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = cardOutline.copy(alpha = 0.5f))

            // Row Kredensial (Room ID & Password)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactCredentialItem(
                    label = "Room ID",
                    value = workspace.id,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Room ID", workspace.id) }
                )
                CompactCredentialItem(
                    label = "Password",
                    value = workspace.password,
                    modifier = Modifier.weight(1f),
                    onCopy = { copyToClipboard(context, "Password", workspace.password) }
                )
            }
        }
    }
}

@Composable
fun CompactCredentialItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit
) {
    Surface(
        onClick = onCopy,
        modifier = modifier.height(54.dp),
        color = lightBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                    value,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = maroonPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ContentCopy, null, tint = maroonPrimary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val statuses = listOf("To Do", "In Progress", "To Verify", "Done")

    val color = when (status) {
        "To Do" -> Color(0xFF64748B)
        "In Progress" -> Color(0xFF3B82F6)
        "To Verify" -> Color(0xFFF59E0B)
        "Done" -> Color(0xFF10B981)
        else -> maroonPrimary
    }

    Box {
        Surface(
            onClick = { expanded = true },
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    status.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = color.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).width(160.dp).border(1.dp, cardOutline, RoundedCornerShape(12.dp))
        ) {
            statuses.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if(s == status) maroonPrimary else Color.DarkGray) },
                    onClick = {
                        onStatusChange(s)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DetailFormCard(
    department: String,
    details: String,
    onUpdate: (String, String) -> Unit
) {
    var deptState by remember(department) { mutableStateOf(department) }
    var descState by remember(details) { mutableStateOf(details) }
    var isEditing by remember { mutableStateOf(department.isEmpty() && details.isEmpty()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, cardOutline)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = "DETAIL TUGAS", icon = Icons.Default.Assignment)

                IconButton(
                    onClick = { isEditing = !isEditing },
                    modifier = Modifier.size(32.dp).background(maroonPrimary.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                        null,
                        tint = maroonPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isEditing) {
                OutlinedTextField(
                    value = deptState,
                    onValueChange = { deptState = it },
                    label = { Text("Departemen Bertugas") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = goldAccent),
                    leadingIcon = { Icon(Icons.Default.Business, null, tint = maroonPrimary, modifier = Modifier.size(18.dp)) }
                )

                OutlinedTextField(
                    value = descState,
                    onValueChange = { descState = it },
                    label = { Text("Deskripsi Task") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = goldAccent),
                    leadingIcon = { Icon(Icons.Default.Notes, null, tint = maroonPrimary, modifier = Modifier.size(18.dp)) }
                )

                Button(
                    onClick = {
                        onUpdate(deptState, descState)
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("SIMPAN DETAIL", color = goldAccent, fontWeight = FontWeight.Black)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(lightBg)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text("DEPARTEMEN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 0.5.sp)
                        Text(department.ifEmpty { "Belum ditentukan" }, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = maroonPrimary)
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(cardOutline))
                    Column {
                        Text("DESKRIPSI TUGAS", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.Gray, letterSpacing = 0.5.sp)
                        Text(details.ifEmpty { "Tidak ada deskripsi tersedia." }, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressDetailCard(
    progressList: List<ProgressDetail>,
    onAdd: (String) -> Unit,
    onEdit: (ProgressDetail) -> Unit,
    onDelete: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var editItem by remember { mutableStateOf<ProgressDetail?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, cardOutline)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionHeader(title = "RIWAYAT PROGRESS", icon = Icons.Default.AutoGraph)

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Update progress terbaru...") },
                trailingIcon = {
                    Surface(
                        onClick = { if (text.isNotBlank()) { onAdd(text); text = "" } },
                        color = if(text.isNotBlank()) maroonPrimary else Color.Transparent,
                        shape = RoundedCornerShape(12.dp),
                        enabled = text.isNotBlank()
                    ) {
                        Icon(
                            Icons.Default.Send,
                            null,
                            tint = if(text.isNotBlank()) goldAccent else Color.LightGray,
                            modifier = Modifier.padding(8.dp).size(20.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = maroonPrimary)
            )

            if (progressList.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada catatan progress.", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    progressList.forEach { progress ->
                        ProgressItem(
                            progress = progress,
                            onEdit = { editItem = it },
                            onDelete = { onDelete(progress.id) }
                        )
                    }
                }
            }
        }
    }

    // --- Edit Dialog ---
    if (editItem != null) {
        var editText by remember { mutableStateOf(editItem!!.text) }
        AlertDialog(
            onDismissRequest = { editItem = null },
            title = { Text("Edit Catatan Progress", fontWeight = FontWeight.Black, color = maroonPrimary) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = maroonPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = { onEdit(editItem!!.copy(text = editText)); editItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = maroonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("UPDATE", fontWeight = FontWeight.Bold, color = goldAccent) }
            },
            dismissButton = {
                TextButton(onClick = { editItem = null }) { Text("BATAL", color = Color.Gray) }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ProgressItem(
    progress: ProgressDetail,
    onEdit: (ProgressDetail) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = lightBg,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, cardOutline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = progress.text,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
                Row {
                    IconButton(onClick = { onEdit(progress) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, null, tint = goldAccent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, null, tint = negativeRed.copy(0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            val dateString = remember(progress.createdAt) {
                try {
                    SimpleDateFormat("EEEE, dd MMM • HH:mm", Locale.getDefault()).format(Date(progress.createdAt))
                } catch (e: Exception) { "Tidak diketahui" }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Text(text = dateString, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }
    }
}

// Helper untuk menyalin ke clipboard
fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label disalin!", Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
fun WorkspaceDetailScreenPreview() {
    WorkspaceDetailContent(
        workspace = Workspace(
            id = "992102",
            name = "Stratify Design Team",
            status = "In Progress",
            department = "UI/UX Engineering",
            details = "Merancang antarmuka aplikasi Android dengan standar premium Material 3.",
            password = "ADMIN-2025",
            members = arrayListOf("Andi Wijaya", "Siti Aminah", "Rizky Fauzi"),
            isJoined = true,
            progressDetails = arrayListOf<ProgressDetail>()
        ),
        onBackPressed = {},
        onUpdateWorkspace = {},
        onDeleteWorkspace = {},
        onAddProgress = {},
        onUpdateProgress = {},
        onDeleteProgress = {}
    )
}