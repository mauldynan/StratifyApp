package com.example.stratify

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stratify.ui.dashboard.DashboardScreen
import com.example.stratify.ui.full_analysis.FullAnalysisScreen
import com.example.stratify.view.profile.ProfileEditScreen
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.view.user.ProfileOptionsScreen
import com.google.firebase.auth.FirebaseAuth

// Definisi Rute Layar
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FullAnalysis : Screen("full_analysis")
    object ProfileOptions : Screen("profile_options")
    object ProfileEdit : Screen("profile_edit")
    object Login : Screen("login")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StratifyTheme {
                // Memanggil Navigasi Utama
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        // --- DASHBOARD ---
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToFullAnalysis = {
                    navController.navigate(Screen.FullAnalysis.route)
                },
                onNavigateToProfileEdit = {
                    // Arahkan ke Menu Opsi Profil dulu
                    navController.navigate(Screen.ProfileOptions.route)
                }
            )
        }

        // --- FULL ANALYSIS ---
        composable(Screen.FullAnalysis.route) {
            FullAnalysisScreen()
        }

        // --- PROFILE OPTIONS (Menu Profil) ---
        composable(Screen.ProfileOptions.route) {
            ProfileOptionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditProfileClicked = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onLogoutClicked = {
                    auth.signOut()
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()

                    // Logic Logout: Kembali ke Login Activity & Hapus Stack
                    // Perlu cast context ke Activity untuk finish() atau start intent manual
                    // Disini kita biarkan toast dulu atau gunakan LocalContext untuk start LoginActivity
                    // (Opsional: implementasi logout intent disini)
                }
            )
        }

        // --- PROFILE EDIT (Form Edit) ---
        composable(Screen.ProfileEdit.route) {
            ProfileEditScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onProfileUpdated = { name, _ ->
                    Toast.makeText(context, "Welcome back, $name", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}