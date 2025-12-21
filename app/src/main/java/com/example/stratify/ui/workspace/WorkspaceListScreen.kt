package com.example.stratify.ui.workspace

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.view.profile.SharedViewModel
import kotlinx.coroutines.launch

// --- Optimized Design Tokens ---
private val maroonPrimary = Color(0xFF760000)
private val goldAccent = Color(0xFFF6C761)
private val lightBg = Color(0xFFF8F9FB)
private val statusGreen = Color(0xFF10B981)

@Composable
fun WorkspaceListScreen(
    viewModel: SharedViewModel,
    onNavigateToWorkspaceDetail: (workspaceId: String) -> Unit,
    onNavigateToCreateWorkspace: () -> Unit,
    onNavigateToJoinWorkspace: () -> Unit
) {
    WorkspaceListContent(
        viewModel = viewModel,
        workspaces = viewModel.workspaces,
        onNavigateToWorkspaceDetail = onNavigateToWorkspaceDetail,
        onNavigateToCreateWorkspace = onNavigateToCreateWorkspace,
        onNavigateToJoinWorkspace = onNavigateToJoinWorkspace
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceListContent(
    viewModel: SharedViewModel,
    workspaces: List<Workspace>,
    onNavigateToWorkspaceDetail: (workspaceId: String) -> Unit,
    onNavigateToCreateWorkspace: () -> Unit,
    onNavigateToJoinWorkspace: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredWorkspaces = workspaces.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState, modifier = Modifier.navigationBarsPadding().padding(bottom = 98.dp)) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 98.dp)) {
                // Dropdown diletakkan di atas FAB agar tidak terhalang
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White).width(200.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Workspace", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.AddCircle, null, tint = maroonPrimary) },
                        onClick = {
                            showMenu = false
                            onNavigateToCreateWorkspace()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Join Workspace", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Default.GroupAdd, null, tint = goldAccent) },
                        onClick = {
                            showMenu = false
                            onNavigateToJoinWorkspace()
                        }
                    )
                }

                FloatingActionButton(
                    onClick = { showMenu = !showMenu },
                    containerColor = maroonPrimary,
                    contentColor = goldAccent,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = if (showMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Menu"
                    )
                }
            }
        },
        containerColor = lightBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- Modern Search Bar ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { Text("Search your workspace...", color = Color.Gray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = maroonPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = goldAccent,
                    unfocusedBorderColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = maroonPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (workspaces.isEmpty() && searchQuery.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GridView, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(12.dp))
                        Text("No Workspaces Yet", fontWeight = FontWeight.Bold, color = Color.Gray)
                        Text("Tap + to start collaborating.", fontSize = 12.sp, color = Color.LightGray)
                    }
                }
            } else {
                Text(
                    "Active Rooms (${filteredWorkspaces.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = maroonPrimary.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    items(filteredWorkspaces, key = { it.id }) { workspace ->
                        WorkspaceCard(
                            workspace = workspace,
                            onClick = { onNavigateToWorkspaceDetail(workspace.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkspaceCard(
    workspace: Workspace,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room Icon/Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(maroonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workspace.name.take(1).uppercase(),
                    color = goldAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = workspace.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = maroonPrimary
                    )

                    // Status Badge
                    Surface(
                        color = statusGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = statusGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Group,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "5 Members Joined", // Mock data for UI
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "• ID: ${workspace.id}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkspaceListScreenPreview() {
    WorkspaceListContent(
        viewModel = SharedViewModel(),
        workspaces = listOf(
            Workspace(id = "992102", name = "Stratify Team"),
            Workspace(id = "441290", name = "Marketing Dept")
        ),
        onNavigateToWorkspaceDetail = {},
        onNavigateToCreateWorkspace = {},
        onNavigateToJoinWorkspace = {}
    )
}