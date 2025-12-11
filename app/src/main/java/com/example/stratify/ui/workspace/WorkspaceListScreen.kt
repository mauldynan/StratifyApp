package com.example.stratify.ui.workspace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.stratify.Workspace
import com.example.stratify.view.profile.SharedViewModel

// 1. Define the Maroon Color
val MaroonPrimary = Color(0xFF800000)

@Composable
fun WorkspaceListScreen(
    viewModel: SharedViewModel,
    onNavigateToWorkspaceDetail: (workspaceId: String) -> Unit
) {
    WorkspaceListContent(
        workspaces = viewModel.workspaces,
        onNavigateToWorkspaceDetail = onNavigateToWorkspaceDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceListContent(
    workspaces: List<Workspace>,
    onNavigateToWorkspaceDetail: (workspaceId: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspaces", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    // 2. Apply Maroon to the Top Bar
                    containerColor = MaroonPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (workspaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No workspaces yet. Create one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "My Workspaces",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(workspaces) { workspace ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        onClick = { onNavigateToWorkspaceDetail(workspace.id) },
                        // 3. Apply Maroon to the Card Border (optional, but looks consistent)
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

@Preview(showBackground = true)
@Composable
fun WorkspaceListScreenPreview() {
    WorkspaceListContent(
        workspaces = listOf(
            Workspace(id = "1", name = "Workspace 1"),
            Workspace(id = "2", name = "Workspace 2")
        ),
        onNavigateToWorkspaceDetail = {}
    )
}
