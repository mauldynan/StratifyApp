package com.example.stratify

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.stratify.ui.workspace.CreateWorkspaceScreen
import com.example.stratify.ui.workspace.JoinWorkspaceScreen
import com.example.stratify.ui.workspace.WorkspaceDetailScreen
import com.example.stratify.ui.workspace.WorkspaceListScreen
import com.example.stratify.ui.workspace.WorkspaceScreen
import com.example.stratify.view.profile.SharedViewModel
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val displayName by viewModel.displayName.observeAsState(initial = "")
    val photoUri by viewModel.photoUri.observeAsState(initial = null)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawerContent(
                displayName = displayName,
                photoUri = photoUri,
                onLogoutClick = {
                    // TODO: Handle logout
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Workspace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textYellow
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = maroonPrimary
                    )
                )
            }
        ) { paddingValues ->
            WorkspaceNavHost(
                modifier = Modifier.padding(paddingValues),
                startDestination = startDestination,
                openDrawer = {
                    scope.launch { drawerState.open() }
                },
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ProfileDrawerContent(displayName: String, photoUri: Uri?, onLogoutClick: () -> Unit) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlideImage(
                model = photoUri,
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                loading = placeholder {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Loading Avatar",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                failure = placeholder {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Default Avatar",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = displayName, style = MaterialTheme.typography.titleMedium)
        }
        Divider()
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogoutClick
        )
    }
}

@Composable
private fun WorkspaceNavHost(
    modifier: Modifier = Modifier,
    startDestination: String,
    openDrawer: () -> Unit,
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
        composable(WorkspaceScreen.WorkspaceList.route) {
            WorkspaceListScreen(
                viewModel = viewModel,
                onNavigateToWorkspaceDetail = { workspaceId ->
                    navController.navigate(WorkspaceScreen.WorkspaceDetail.createRoute(workspaceId))
                },
                onOpenDrawer = openDrawer
            )
        }
        composable(
            route = WorkspaceScreen.WorkspaceDetail.route,
            arguments = WorkspaceScreen.WorkspaceDetail.navArguments
        ) {
            val workspaceId = it.arguments?.getString("workspaceId")
            WorkspaceDetailScreen(
                workspaceId = workspaceId,
                onBackPressed = { navController.navigateUp() }
            )
        }
    }
}
