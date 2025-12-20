package com.example.stratify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
private val goldAccent = Color(0xFFEBC05C)

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Outlined.WorkOutline,
                    contentDescription = null,
                    tint = goldAccent,
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                WorkspaceOptionCard(
                    title = "Create Workspace",
                    description = "Be an admin and manage your team.",
                    icon = Icons.Filled.Add,
                    iconBackgroundColor = maroonPrimary,
                    iconContentColor = textYellow,
                    onClick = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                WorkspaceOptionCard(
                    title = "Join Workspace",
                    description = "Use your colleague's code.",
                    icon = Icons.Filled.Group,
                    iconBackgroundColor = goldAccent,
                    iconContentColor = maroonPrimary,
                    onClick = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }
                )

                Spacer(modifier = Modifier.height(32.dp))
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

@Composable
fun WorkspaceOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconContentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconContentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = maroonPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
