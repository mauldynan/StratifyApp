package com.example.stratify

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stratify.ui.dashboard.DashboardScreen
import com.example.stratify.ui.full_analysis.FullAnalysisScreen
import com.example.stratify.ui.main.BottomNavigationBar
import com.example.stratify.ui.scrum.ScrumScreen
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.view.profile.ProfileEditScreen
import com.example.stratify.view.profile.SharedViewModel
import com.example.stratify.view.user.ProfileOptionsScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FullAnalysis : Screen("full_analysis")
    object Scrum : Screen("scrum")
    object MainWorkspace : Screen("main_workspace")
    object ProfileOptions : Screen("profile_options")
    object ProfileEdit : Screen("profile_edit")
    object Login : Screen("login")
}

class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StratifyTheme {
                AppNavigation(viewModel = sharedViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: SharedViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }

            composable(Screen.FullAnalysis.route) {
                FullAnalysisScreen(onBackPressed = { navController.popBackStack() })
            }

            composable(Screen.Scrum.route) {
                ScrumScreen()
            }

            composable(Screen.MainWorkspace.route) {
                MainWorkspaceScreen(viewModel = viewModel)
            }

            composable(Screen.ProfileOptions.route) {
                ProfileOptionsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onEditProfileClicked = { navController.navigate(Screen.ProfileEdit.route) },
                    onLogoutClicked = {
                        auth.signOut()
                        Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            composable(Screen.ProfileEdit.route) {
                ProfileEditScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProfileUpdated = { name, _ ->
                        Toast.makeText(context, "Welcome back, $name", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
