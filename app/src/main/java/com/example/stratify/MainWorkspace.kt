package com.example.stratify

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.stratify.data.Workspace
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.ui.workspace.CreateWorkspaceScreen
import com.example.stratify.ui.workspace.JoinWorkspaceScreen
import com.example.stratify.ui.workspace.WorkspaceDetailScreen
import com.example.stratify.ui.workspace.WorkspaceListScreen
import com.example.stratify.ui.workspace.WorkspaceScreen
import com.example.stratify.view.profile.SharedViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class MainWorkspace : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = getStartDestination(this)

        setContent {
            StratifyTheme {
                WorkspaceApp(startDestination = startDestination, viewModel = sharedViewModel)
            }
        }
    }

    private fun getStartDestination(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("workspace_list_json", null)
        return if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<Workspace>>() {}.type
            val workspaceList: List<Workspace> = Gson().fromJson(json, type)
            if (workspaceList.isNotEmpty()) WorkspaceScreen.WorkspaceList.route else WorkspaceScreen.Start.route
        } else {
            WorkspaceScreen.Start.route
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceApp(startDestination: String, viewModel: SharedViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val displayName = viewModel.displayName.observeAsState(initial = "")
    val photoUri = viewModel.photoUri.observeAsState(initial = null)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ProfileDrawerContent(
                displayName = displayName.value,
                photoUri = photoUri.value,
                onLogoutClick = {
                    // TODO: Handle logout
                }
            )
        }
    ) {
        WorkspaceNavHost(
            startDestination = startDestination,
            openDrawer = {
                scope.launch { drawerState.open() }
            }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ProfileDrawerContent(displayName: String, photoUri: Uri?, onLogoutClick: () -> Unit) {
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
fun WorkspaceNavHost(startDestination: String, openDrawer: () -> Unit) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(WorkspaceScreen.Start.route) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) }) {
                    Text("Create Workspace")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }) {
                    Text("Join Workspace")
                }
            }
        }
        composable(WorkspaceScreen.CreateWorkspace.route) {
            CreateWorkspaceScreen(
                onWorkspaceCreated = {
                    navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onBackPressed = { navController.navigateUp() }
            )
        }
        composable(WorkspaceScreen.JoinWorkspace.route) {
            JoinWorkspaceScreen(
                onWorkspaceJoined = { navController.navigate(WorkspaceScreen.WorkspaceList.route) },
                onBackPressed = { navController.navigateUp() }
            )
        }
        composable(WorkspaceScreen.WorkspaceList.route) {
            WorkspaceListScreen(
                onNavigateToStart = { navController.navigate(WorkspaceScreen.Start.route) },
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
