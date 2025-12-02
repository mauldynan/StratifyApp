package com.example.stratify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stratify.ui.dashboard.DashboardScreen
import com.example.stratify.ui.full_analysis.FullAnalysisScreen
import com.example.stratify.ui.theme.StratifyTheme

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FullAnalysis : Screen("full_analysis")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StratifyTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(onNavigateToFullAnalysis = {
                navController.navigate(Screen.FullAnalysis.route)
            })
        }
        composable(Screen.FullAnalysis.route) {
            FullAnalysisScreen()
        }
    }
}
