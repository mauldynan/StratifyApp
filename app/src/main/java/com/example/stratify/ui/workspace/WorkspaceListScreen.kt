package com.example.stratify.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun WorkspaceListScreen(
    viewModel: SharedViewModel,
    onNavigateToWorkspaceDetail: (workspaceId: String) -> Unit,
    onNavigateToCreateWorkspace: () -> Unit,
    onNavigateToJoinWorkspace: () -> Unit
) {
    WorkspaceListContent(
        workspaces = viewModel.workspaces,
        onNavigateToWorkspaceDetail = onNavigateToWorkspaceDetail,
        onNavigateToCreateWorkspace = onNavigateToCreateWorkspace,
        onNavigateToJoinWorkspace = onNavigateToJoinWorkspace
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceListContent(
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

    Scaffold(
        floatingActionButton = {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
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
                        leadingIcon = { Icon(Icons.Outlined.Group, null, tint = goldAccent) },
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your workspace...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = maroonPrimary,
                    focusedLabelColor = maroonPrimary,
                    cursorColor = maroonPrimary
                )
            )

            Spacer(Modifier.height(24.dp))

            // Content Logic
            if (filteredWorkspaces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Matching Workspaces", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 140.dp)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(maroonPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = workspace.name.take(1).uppercase(),
                    color = goldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workspace.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))

                Text(
                    text = "ID: ${workspace.id}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (!workspace.status.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workspace.status,
                        fontSize = 10.sp,
                        color = if (workspace.status == "Done") Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkspaceListPreview() {
    WorkspaceListContent(
        workspaces = listOf(
            Workspace(id = "992102", name = "Stratify Team", status = "In Progress"),
            Workspace(id = "441290", name = "Marketing Dept", status = "Done")
        ),
        onNavigateToWorkspaceDetail = {},
        onNavigateToCreateWorkspace = {},
        onNavigateToJoinWorkspace = {}
    )
}