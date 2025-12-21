package com.example.stratify

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.*
import com.example.stratify.ui.dashboard.DashboardScreen
import com.example.stratify.ui.full_analysis.FullAnalysisScreen
import com.example.stratify.ui.main.BottomNavigationBar
import com.example.stratify.ui.scrum.ScrumScreen
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.view.profile.ProfileEditScreen
import com.example.stratify.view.profile.SharedViewModel
import com.example.stratify.view.user.ProfileOptionsScreen
import com.google.firebase.auth.FirebaseAuth

// =====================
// Routes
// =====================
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FullAnalysis : Screen("full_analysis")
    object Scrum : Screen("scrum")
    object MainWorkspace : Screen("main_workspace")
    object ProfileOptions : Screen("profile_options")
    object EditProfile : Screen("edit_profile")
    object Login : Screen("login")
}

class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StratifyTheme {
                AppRoot(viewModel = sharedViewModel)
            }
        }
    }
}

@Composable
fun AppRoot(viewModel: SharedViewModel) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute !in listOf(
        Screen.ProfileOptions.route,
        Screen.EditProfile.route,
        Screen.Login.route
    ) && !viewModel.isCompareMode.value

    // ❌ Scaffold TIDAK punya bottomBar
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0) // ⬅️ HILANGKAN SAFE AREA BAWAAN
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E7EB)) // background screen
                .padding(padding)
        ) {

            // =====================
            // NAV CONTENT
            // =====================
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {

                composable(Screen.Dashboard.route) {
                    DashboardScreen(navController, viewModel)
                }

                composable(Screen.FullAnalysis.route) {
                    FullAnalysisScreen(
                        navController = navController,
                        onBackPressed = { navController.popBackStack() }
                    )
                }

                composable(Screen.Scrum.route) {
                    ScrumScreen()
                }

                composable(Screen.MainWorkspace.route) {
                    MainWorkspaceScreen(viewModel)
                }

                dialog(
                    route = Screen.ProfileOptions.route,
                    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    ProfileOptionsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onEditProfileClicked = {
                            navController.navigate(Screen.EditProfile.route)
                        },
                        onLogoutClicked = {
                            auth.signOut()
                            Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()

                            context.startActivity(
                                Intent(context, LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                            )
                        }
                    )
                }

                composable(Screen.EditProfile.route) {
                    ProfileEditScreen(
                        sharedViewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onProfileUpdated = { name, _ ->
                            Toast.makeText(context, "Welcome back, $name", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // =====================
            // FLOATING BOTTOM NAV (OVERLAY)
            // =====================
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BottomNavigationBar(navController)
                }
            }
        }
    }
}
