
package com.example.stratify.ui.workspace

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument

sealed class WorkspaceScreen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Start : WorkspaceScreen(
        route = "start",
        navArguments = listOf(navArgument("showBackButton") { defaultValue = false })
    )

    data object CreateWorkspace : WorkspaceScreen("createWorkspace")
    data object JoinWorkspace : WorkspaceScreen("joinWorkspace")
    data object WorkspaceList : WorkspaceScreen("workspaceList")

    data object WorkspaceDetail : WorkspaceScreen(
        route = "workspaceDetail/{workspaceId}",
        navArguments = listOf(navArgument("workspaceId") { })
    ) {
        fun createRoute(workspaceId: String) = "workspaceDetail/$workspaceId"
    }
}
