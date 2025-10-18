package com.example.stratify

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainWorkspace : AppCompatActivity() {
    /**
     * Initializes the activity, sets up the navigation, and determines the start destination.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_workspace)

        // Access SharedPreferences to retrieve saved workspace data.
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("workspace_list_json", null)
        val type = object : TypeToken<List<Workspace>>() {}.type
        val workspaceList: List<Workspace>? = gson.fromJson(json, type)

        // Check if the workspace list is not null and not empty.
        val hasWorkspace = !workspaceList.isNullOrEmpty()

        // Get the NavController from the NavHostFragment.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_workspace)

        // Set the start destination of the navigation graph based on whether a workspace exists.
        if (hasWorkspace) {
            navGraph.setStartDestination(R.id.workspaceListFragment)
        } else {
            navGraph.setStartDestination(R.id.startFragment)
        }

        // Set the modified navigation graph to the NavController.
        navController.graph = navGraph
    }
}
