package com.example.stratify.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stratify.Workspace
import com.example.stratify.view.profile.SharedViewModel
import kotlinx.coroutines.launch

val MaroonPrimary = Color(0xFF800000)

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            // Lowered FAB significantly
            Box(modifier = Modifier
                .padding(bottom = 0.dp)
                .offset(y = 40.dp)) {
                FloatingActionButton(
                    onClick = { showMenu = true },
                    containerColor = MaroonPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Workspace", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Workspace") },
                        onClick = {
                            showMenu = false
                            onNavigateToCreateWorkspace()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Join Workspace") },
                        onClick = {
                            showMenu = false
                            onNavigateToJoinWorkspace()
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (workspaces.isEmpty() && searchQuery.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No workspaces yet. Create or join one!")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp) // Increased height only
                        .offset(y = (-10).dp), // Moved up
                    placeholder = { Text("Search", color = Color(0xFFB0B0B0)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaroonPrimary,
                        unfocusedBorderColor = Color.LightGray,
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredWorkspaces, key = { it.id }) { workspace ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteWorkspace(workspace)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Workspace deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDeleteWorkspace()
                                        }
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.White
                                    )
                                }
                            }
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                onClick = { onNavigateToWorkspaceDetail(workspace.id) },
                                border = BorderStroke(1.dp, MaroonPrimary)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = workspace.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Code: ${workspace.id}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkspaceListScreenPreview() {
    WorkspaceListContent(
        viewModel = SharedViewModel(),
        workspaces = listOf(
            Workspace(id = "1", name = "Workspace 1"),
            Workspace(id = "2", name = "Workspace 2")
        ),
        onNavigateToWorkspaceDetail = {},
        onNavigateToCreateWorkspace = {},
        onNavigateToJoinWorkspace = {}
    )
}
