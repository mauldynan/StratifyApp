package com.example.stratify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
// IMPORT PENTING UNTUK NAVIGASI ARGUMENT
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.work.*

import com.example.stratify.ui.dashboard.DashboardScreen
// Pastikan tidak ada import FullAnalysisScreen yang lama jika sudah dihapus
// import com.example.stratify.ui.full_analysis.FullAnalysisScreen <--- HAPUS JIKA MERAH

import com.example.stratify.ui.main.BottomNavigationBar
import com.example.stratify.ui.scrum.ScrumScreen
import com.example.stratify.ui.theme.StratifyTheme
import com.example.stratify.view.profile.ProfileEditScreen
import com.example.stratify.view.profile.SharedViewModel
import com.example.stratify.view.user.ProfileOptionsScreen
import com.example.scrum_section.worker.DailySummaryWorker
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

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

        // =====================
        // 🔔 REQUEST NOTIFICATION PERMISSION (Android 13+)
        // =====================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        // =====================
        // ⏰ SCHEDULE DAILY WORKER
        // =====================
        val dailyWork =
            PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailySummaryWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWork
        )

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
        Screen.Login.route,
        // Sembunyikan bottom bar saat di halaman analisis agar fokus
        "${Screen.FullAnalysis.route}/{appName}"
    )

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E7EB))
                .padding(padding)
        ) {

            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route
            ) {

                composable(Screen.Dashboard.route) {
                    DashboardScreen(navController, viewModel)
                }

                // ==========================================
                // 👇 BAGIAN INTEGRASI FULL ANALYSIS 👇
                // ==========================================
                composable(
                    route = "${Screen.FullAnalysis.route}/{appName}", // Menerima parameter nama aplikasi
                    arguments = listOf(navArgument("appName") { type = NavType.StringType })
                ) { backStackEntry ->
                    // 1. Tangkap nama aplikasi yang dikirim (misal: "shopee")
                    val appName = backStackEntry.arguments?.getString("appName") ?: "shopee"

                    // 2. Panggil FullAnalysisFragment
                    FullAnalysisFragment(
                        targetApp = appName,
                        onBackPressed = { navController.popBackStack() }
                    )
                }
                // ==========================================

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

            if (showBottomBar) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    BottomNavigationBar(navController)
                }
            }
        }
    }
}