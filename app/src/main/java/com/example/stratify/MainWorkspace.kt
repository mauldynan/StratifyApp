package com.example.stratify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stratify.ui.workspace.CreateWorkspaceScreen
import com.example.stratify.ui.workspace.JoinWorkspaceScreen
import com.example.stratify.ui.workspace.WorkspaceDetailScreen
import com.example.stratify.ui.workspace.WorkspaceListScreen
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

@Composable
private fun WorkspaceApp(startDestination: String, viewModel: SharedViewModel) {
    // The Scaffold and NavDrawer have been removed. Each screen manages its own UI.
    WorkspaceNavHost(
        startDestination = startDestination,
        viewModel = viewModel
    )
}

@Composable
private fun WorkspaceNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    viewModel: SharedViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(WorkspaceScreen.Start.route) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = maroonPrimary,
                        contentColor = textYellow
                    )
                ) {
                    Text("Create Workspace")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = maroonPrimary,
                        contentColor = textYellow
                    )
                ) {
                    Text("Join Workspace")
                }
            }
        }
        composable(WorkspaceScreen.CreateWorkspace.route) {
            CreateWorkspaceScreen(
                onWorkspaceCreated = { name, code, password ->
                    viewModel.createWorkspace(name, code, password)
                    navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                        popUpTo(WorkspaceScreen.Start.route) { inclusive = true }
                    }
                },
                onBackPressed = { navController.navigateUp() }
            )
        }
        composable(WorkspaceScreen.JoinWorkspace.route) {
            JoinWorkspaceScreen(
                onWorkspaceJoined = { code, password ->
                    if (viewModel.joinWorkspace(code, password)) {
                        navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                            popUpTo(WorkspaceScreen.Start.route) { inclusive = true }
                        }
                    }
                },
                onBackPressed = { navController.navigateUp() }
            )
        }
        // In: C:/Users/abrah/Work_Projects/StratifyApp/app/src/main/java/com/example/stratify/MainWorkspace.kt

        composable(WorkspaceScreen.WorkspaceList.route) {
            WorkspaceListScreen(
                viewModel = viewModel,
                onNavigateToWorkspaceDetail = { workspaceId ->
                    navController.navigate(WorkspaceScreen.WorkspaceDetail.createRoute(workspaceId))
                },
                onNavigateToCreateWorkspace = {
                    navController.navigate(WorkspaceScreen.CreateWorkspace.route)
                },
                // --- FIX: Add the missing parameter here ---
                onNavigateToJoinWorkspace = {
                    navController.navigate(WorkspaceScreen.JoinWorkspace.route)
                }
            )
        }

        composable(
            route = WorkspaceScreen.WorkspaceDetail.route,
            arguments = WorkspaceScreen.WorkspaceDetail.navArguments
        ) { backStackEntry ->
            val workspaceId = backStackEntry.arguments?.getString("workspaceId")
            WorkspaceDetailScreen(
                viewModel = viewModel,
                workspaceId = workspaceId,
                onBackPressed = { navController.navigateUp() }
            )
        }
    }
}
