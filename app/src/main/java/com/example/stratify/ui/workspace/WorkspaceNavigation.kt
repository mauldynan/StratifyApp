package com.example.stratify.ui.workspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NamedNavArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stratify.view.profile.SharedViewModel



// --- Colors ---
private val maroonPrimary = Color(0xFF800000)
private val textYellow = Color(0xFFFFEB3B)

sealed class WorkspaceScreen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Start : WorkspaceScreen(
        route = "start",
        navArguments = listOf(navArgument("showBackButton") { defaultValue = false })
    )

    data object CreateWorkspace : WorkspaceScreen("createWorkspace")
    data object JoinWorkspace : WorkspaceScreen("joinWorkspace")
    data object WorkspaceList : WorkspaceScreen("workspaceList")

    data object WorkspaceDetail : WorkspaceScreen(
        route = "workspaceDetail/{workspaceId}",
        navArguments = listOf(navArgument("workspaceId") { })
    ) {
        fun createRoute(workspaceId: String) = "workspaceDetail/$workspaceId"
    }
}

@Composable
fun WorkspaceNavHost(
    modifier: Modifier = Modifier,
    startDestination: String
) {
    val navController = rememberNavController()

    val viewModel: SharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()



    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(WorkspaceScreen.Start.route) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                    modifier = Modifier
                        .width(250.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = maroonPrimary,
                        contentColor = textYellow
                    )
                ) {
                    Text("Create Workspace", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) },
                    modifier = Modifier
                        .width(250.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = maroonPrimary,
                        contentColor = textYellow
                    )
                ) {
                    Text("Join Workspace", fontSize = 18.sp)
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
                    viewModel.joinWorkspace(code, password)

                    navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                        popUpTo(WorkspaceScreen.Start.route) { inclusive = true }
                    }
                },
                        onBackPressed = { navController.navigateUp() }
            )
        }

        composable(WorkspaceScreen.WorkspaceList.route) {
            WorkspaceListScreen(
                viewModel = viewModel,
                onNavigateToWorkspaceDetail = { workspaceId ->
                    navController.navigate(WorkspaceScreen.WorkspaceDetail.createRoute(workspaceId))
                },
                onNavigateToCreateWorkspace = {
                    navController.navigate(WorkspaceScreen.CreateWorkspace.route)
                },
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

@Preview(showBackground = true)
@Composable
fun WorkspaceNavHostPreview() {
    WorkspaceNavHost(
        startDestination = WorkspaceScreen.Start.route
    )
}

