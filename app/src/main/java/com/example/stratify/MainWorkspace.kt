
package com.example.stratify

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stratify.data.Workspace
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.ui.workspace.CreateWorkspaceScreen
import com.example.stratify.ui.workspace.JoinWorkspaceScreen
import com.example.stratify.ui.workspace.StartScreen
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

    // Observe the ViewModel states
    val displayName = viewModel.displayName.observeAsState(initial = "")
    val photoUri = viewModel.photoUri.observeAsState(initial = null)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Your Drawer Content Composable will go here
            // For now, it is empty as per the migration scope.
        }
    ) {
        WorkspaceNavHost(startDestination = startDestination)
    }
}

@Composable
fun WorkspaceNavHost(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(WorkspaceScreen.Start.route) {
            StartScreen(
                onNavigateToCreateWorkspace = { navController.navigate(WorkspaceScreen.CreateWorkspace.route) },
                onNavigateToJoinWorkspace = { navController.navigate(WorkspaceScreen.JoinWorkspace.route) }
            )
        }
        composable(WorkspaceScreen.CreateWorkspace.route) {
            CreateWorkspaceScreen(
                onWorkspaceCreated = {
                    navController.navigate(WorkspaceScreen.WorkspaceList.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
        composable(WorkspaceScreen.JoinWorkspace.route) {
            JoinWorkspaceScreen(
                onWorkspaceJoined = { navController.navigate(WorkspaceScreen.WorkspaceList.route) }
            )
        }
        composable(WorkspaceScreen.WorkspaceList.route) {
            WorkspaceListScreen(
                onNavigateToStart = { navController.navigate(WorkspaceScreen.Start.route) },
                onNavigateToWorkspaceDetail = { workspaceId ->
                    navController.navigate(WorkspaceScreen.WorkspaceDetail.createRoute(workspaceId))
                }
            )
        }
        composable(
            route = WorkspaceScreen.WorkspaceDetail.route,
            arguments = WorkspaceScreen.WorkspaceDetail.navArguments
        ) {
            val workspaceId = it.arguments?.getString("workspaceId")
            WorkspaceDetailScreen(workspaceId = workspaceId)
        }
    }
}
