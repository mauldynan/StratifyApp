package com.example.stratify

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stratify.ui.workspace.CreateWorkspaceScreen
import com.example.stratify.ui.workspace.JoinWorkspaceScreen
import com.example.stratify.ui.workspace.WorkspaceDetailScreen
import com.example.stratify.ui.workspace.WorkspaceListScreen
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

// --- Colors ---
private val MaroonPrimary = Color(0xFF8B0000)
private val TextYellow = Color(0xFFF6C761)
private val GoldAccent = Color(0xFFEBC05C)
private val LightGrayBg = Color(0xFFF8F9FB)

// --- Navigation Destinations ---
sealed class WorkspaceScreen(val route: String) {
    data object CreateWorkspace : WorkspaceScreen("create_workspace")
    data object JoinWorkspace : WorkspaceScreen("join_workspace")
    data object WorkspaceList : WorkspaceScreen("workspace_list")
    data object WorkspaceDetail : WorkspaceScreen("workspace_detail/{workspaceId}") {
        fun createRoute(workspaceId: String) = "workspace_detail/$workspaceId"
        val navArguments = listOf(navArgument("workspaceId") { type = NavType.StringType })
    }
}

// --- Main Screen ---
@Composable
fun MainWorkspaceScreen(
    viewModel: SharedViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit = {}
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            viewModel.loadUserWorkspaces()
        }
    }

    WorkspaceNavHost(
        viewModel = viewModel,
        onBottomBarVisibilityChange = onBottomBarVisibilityChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceNavHost(
    viewModel: SharedViewModel,
    navController: NavHostController = rememberNavController(),
    onBottomBarVisibilityChange: (Boolean) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Logic to Hide/Show Bottom Bar
    LaunchedEffect(currentRoute) {
        val shouldHideBottomBar = currentRoute?.startsWith("workspace_detail") == true ||
                currentRoute == WorkspaceScreen.CreateWorkspace.route ||
                currentRoute == WorkspaceScreen.JoinWorkspace.route

        // Pass 'true' to SHOW, 'false' to HIDE
        onBottomBarVisibilityChange(!shouldHideBottomBar)
    }

    Scaffold(
        topBar = {
            // FIX: Only show "Workspace" Top Bar on the List screen.
            if (currentRoute == WorkspaceScreen.WorkspaceList.route) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Workspace",
                            fontWeight = FontWeight.Bold,
                            color = TextYellow
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaroonPrimary
                    )
                )
            }
        },
        containerColor = LightGrayBg
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WorkspaceScreen.WorkspaceList.route,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {

            // 1. WORKSPACE LIST
            composable(WorkspaceScreen.WorkspaceList.route) {
                when {
                    viewModel.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaroonPrimary)
                        }
                    }
                    viewModel.workspaces.isEmpty() -> {
                        StartWorkspaceUI(
                            onCreateClick = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                            onJoinClick = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }
                        )
                    }
                    else -> {
                        WorkspaceListScreen(
                            viewModel = viewModel,
                            onNavigateToWorkspaceDetail = { id ->
                                navController.navigate(WorkspaceScreen.WorkspaceDetail.createRoute(id))
                            },
                            onNavigateToCreateWorkspace = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                            onNavigateToJoinWorkspace = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }
                        )
                    }
                }
            }

            // 2. CREATE WORKSPACE
            composable(WorkspaceScreen.CreateWorkspace.route) {
                CreateWorkspaceScreen(
                    onWorkspaceCreated = { name, code, password ->
                        viewModel.createWorkspace(name, code, password)
                        navController.popBackStack()
                    },
                    onBackPressed = { navController.popBackStack() }
                )
            }

            // 3. JOIN WORKSPACE
            composable(WorkspaceScreen.JoinWorkspace.route) {
                JoinWorkspaceScreen(
                    onWorkspaceJoined = { code, password ->
                        viewModel.joinWorkspace(code, password)
                        navController.popBackStack()
                    },
                    onBackPressed = { navController.popBackStack() }
                )
            }

            // 4. WORKSPACE DETAILS
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

// --- MISSING COMPONENTS DEFINED HERE ---

@Composable
fun StartWorkspaceUI(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit
) {
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
            tint = GoldAccent,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        WorkspaceOptionCard(
            title = "Create Workspace",
            description = "Be an admin and manage your team.",
            icon = Icons.Filled.Add,
            iconBackgroundColor = MaroonPrimary,
            iconContentColor = TextYellow,
            onClick = onCreateClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        WorkspaceOptionCard(
            title = "Join Workspace",
            description = "Use your colleague's code.",
            icon = Icons.Filled.Group,
            iconBackgroundColor = GoldAccent,
            iconContentColor = MaroonPrimary,
            onClick = onJoinClick
        )

        Spacer(modifier = Modifier.height(120.dp))
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

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaroonPrimary
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showSystemUi = true)
@Composable
fun StartWorkspacePreview() {
    MaterialTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Workspace", fontWeight = FontWeight.Bold, color = TextYellow)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaroonPrimary
                    )
                )
            },
            containerColor = LightGrayBg
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                StartWorkspaceUI(onCreateClick = {}, onJoinClick = {})
            }
        }
    }
}