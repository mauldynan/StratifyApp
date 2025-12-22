package com.example.stratify

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stratify.ui.workspace.CreateWorkspaceScreen
import com.example.stratify.ui.workspace.JoinWorkspaceScreen
import com.example.stratify.ui.workspace.WorkspaceDetailScreen
import com.example.stratify.ui.workspace.WorkspaceListScreen
import com.example.stratify.ui.workspace.WorkspaceScreen
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect



// --- Colors (Matched with ScrumScreen) ---
private val maroonPrimary = Color(0xFF8B0000)
private val textYellow = Color(0xFFF6C761)
private val goldAccent = Color(0xFFEBC05C)
private val lightGrayBg = Color(0xFFF8F9FB)

@Composable
fun MainWorkspaceScreen(viewModel: SharedViewModel) {

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            viewModel.loadUserWorkspaces()
        }
    }

    if (viewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        WorkspaceAppContent(
            startDestination = if (viewModel.workspaces.isEmpty())
                WorkspaceScreen.Start.route
            else
                WorkspaceScreen.WorkspaceList.route,
            viewModel = viewModel
        )
    }


    val startDestination = if (viewModel.workspaces.isEmpty()) {
        WorkspaceScreen.Start.route
    } else {
        WorkspaceScreen.WorkspaceList.route
    }

    WorkspaceAppContent(
        startDestination = startDestination,
        viewModel = viewModel
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkspaceAppContent(
    startDestination: String,
    viewModel: SharedViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute != WorkspaceScreen.CreateWorkspace.route &&
                currentRoute != WorkspaceScreen.JoinWorkspace.route
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Workspace",
                            fontWeight = FontWeight.Bold,
                            color = textYellow
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = maroonPrimary
                    )
                )
            }
        },
        containerColor = lightGrayBg
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable(WorkspaceScreen.Start.route) {
                    StartWorkspaceUI(
                        onCreateClick = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                        onJoinClick = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }
                    )
                }

                composable(WorkspaceScreen.CreateWorkspace.route) {
                    CreateWorkspaceScreen(
                        onWorkspaceCreated = { name, code, password ->
                            viewModel.createWorkspace(name, code, password)
                            navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                                popUpTo(WorkspaceScreen.Start.route) { inclusive = true }
                            }
                        },
                        onBackPressed = { navController.popBackStack() }
                    )
                }

                composable(WorkspaceScreen.JoinWorkspace.route) {
                    JoinWorkspaceScreen(
                        onWorkspaceJoined = { code, password ->
                            val isSuccessful = viewModel.joinWorkspace(code, password)
                            if (isSuccessful) {
                                navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                                    popUpTo(WorkspaceScreen.Start.route) { inclusive = true }
                                }
                            }
                        },
                        onBackPressed = { navController.popBackStack() }
                    )
                }

                composable(WorkspaceScreen.WorkspaceList.route) {
                    WorkspaceListScreen(
                        viewModel = viewModel,
                        onNavigateToWorkspaceDetail = { id ->
                            navController.navigate(WorkspaceScreen.WorkspaceDetail.createRoute(id))
                        },
                        onNavigateToCreateWorkspace = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                        onNavigateToJoinWorkspace = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }
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
                        onBackPressed = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun StartWorkspaceUI(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.WorkOutline,
            contentDescription = null,
            tint = goldAccent,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        WorkspaceOptionCard(
            title = "Create Workspace",
            description = "Be an admin and manage your team.",
            icon = Icons.Filled.Add,
            iconBackgroundColor = maroonPrimary,
            iconContentColor = textYellow,
            onClick = onCreateClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        WorkspaceOptionCard(
            title = "Join Workspace",
            description = "Use your colleague's code.",
            icon = Icons.Filled.Group,
            iconBackgroundColor = goldAccent,
            iconContentColor = maroonPrimary,
            onClick = onJoinClick
        )
        Spacer(modifier = Modifier.height(120.dp)) // Padding agar tidak tertutup nav
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
        modifier = Modifier.fillMaxWidth().height(90.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconContentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = maroonPrimary)
                Text(text = description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// --- PREVIEWS (FIXED) ---

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun StartWorkspacePreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Workspace", fontWeight = FontWeight.Bold, color = textYellow) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = maroonPrimary
                    )
                )
            },
            containerColor = lightGrayBg
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                StartWorkspaceUI(onCreateClick = {}, onJoinClick = {})
            }
        }
    }
}
