
package com.example.stratify.ui.workspace

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun WorkspaceListScreen(
    onNavigateToStart: () -> Unit,
    onNavigateToWorkspaceDetail: (workspaceId: String) -> Unit
) {
    Text(text = "Workspace List Screen")
}
