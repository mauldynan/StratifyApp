package com.example.stratify.ui.workspace

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun WorkspaceDetailScreen(
    workspaceId: String?,
    onBackPressed: () -> Unit // This line was added
) {
    // A proper implementation would use this in a back button
    Text(text = "Workspace Detail Screen: $workspaceId")
}