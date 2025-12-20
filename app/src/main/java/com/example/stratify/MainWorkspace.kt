package com.example.stratify

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.stratify.ui.workspace.WorkspaceNavHost
import com.example.stratify.ui.workspace.WorkspaceScreen
import com.example.stratify.view.profile.SharedViewModel

// --- Colors ---
private val maroonPrimary = Color(0xFF800000)
private val textYellow = Color(0xFFFFEB3B)

/**
 * Main composable for the Workspace section. This is the entry point from MainActivity's NavHost.
 */
@Composable
fun MainWorkspaceScreen(viewModel: SharedViewModel) {
    val startDestination = if (viewModel.workspaces.isEmpty()) {
        WorkspaceScreen.Start.route
    } else {
        WorkspaceScreen.WorkspaceList.route
    }

    WorkspaceApp(startDestination = startDestination, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkspaceApp(startDestination: String, viewModel: SharedViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Workspace",
                        color = textYellow,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = maroonPrimary
                )
            )
        }
    ) { innerPadding ->
        WorkspaceNavHost(
            modifier = Modifier.padding(innerPadding),
            startDestination = startDestination,
            viewModel = viewModel
        )
    }
}
