package com.example.stratify

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainWorkspace : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // SharedViewModel untuk profile
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_workspace)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Setup navigation
        setupNavigation()

        // Setup header account icon
        setupHeader()

        // Setup drawer profile
        setupDrawer()

        // Load initial profile
        loadInitialProfile()
    }

    private fun setupNavigation() {
        // Access SharedPreferences to retrieve saved workspace data
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = prefs.getString("workspace_list_json", null)
        val type = object : TypeToken<List<Workspace>>() {}.type
        val workspaceList: List<Workspace>? = gson.fromJson(json, type)

        // Check if the workspace list is not null and not empty
        val hasWorkspace = !workspaceList.isNullOrEmpty()

        // Get the NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_workspace)

        // Set the start destination based on whether a workspace exists
        if (hasWorkspace) {
            navGraph.setStartDestination(R.id.workspaceListFragment)
        } else {
            navGraph.setStartDestination(R.id.startFragment)
        }

        navController.graph = navGraph
    }

    private fun setupHeader() {
        val headerAccountIcon = findViewById<ImageButton>(R.id.account_icon)

        headerAccountIcon?.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // Observe profile changes untuk header
        sharedViewModel.photoUri.observe(this) { uri ->
            Log.d("DBG_WORKSPACE", "Header photoUri changed: $uri")
            headerAccountIcon?.let { icon ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_acc)
                    .circleCrop()
                    .into(icon)
            }
        }
    }

    private fun setupDrawer() {
        val navView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.nav_view)
        val drawerContent = navView?.getChildAt(0)

        // Find views di drawer (sama kayak MainActivity)
        val headerProfileImage = drawerContent?.findViewById<ImageView>(R.id.ivProfileImage)
        val headerName = drawerContent?.findViewById<TextView>(R.id.tvProfileName)
        val headerEmail = drawerContent?.findViewById<TextView>(R.id.tvProfileEmail)

        val optionProfileEdit = drawerContent?.findViewById<TextView>(R.id.optionProfileEdit)
        val optionAccount = drawerContent?.findViewById<TextView>(R.id.optionAccount)
        val accountOptionsContainer = drawerContent?.findViewById<LinearLayout>(R.id.account_options_container)
        val optionLogout = drawerContent?.findViewById<TextView>(R.id.optionLogout)

        // Load initial dari Firebase
        val currentUser = auth.currentUser
        if (sharedViewModel.displayName.value == null && !currentUser?.displayName.isNullOrEmpty()) {
            sharedViewModel.displayName.value = currentUser?.displayName
        }
        if (sharedViewModel.photoUri.value == null && currentUser?.photoUrl != null) {
            sharedViewModel.photoUri.value = currentUser.photoUrl
        }

        headerEmail?.text = currentUser?.email ?: "user.email@example.com"

        // Observe ViewModel changes
        sharedViewModel.displayName.observe(this) { name ->
            Log.d("DBG_WORKSPACE", "Drawer displayName changed: $name")
            headerName?.text = name ?: auth.currentUser?.displayName ?: "User Name"
        }

        sharedViewModel.photoUri.observe(this) { uri ->
            Log.d("DBG_WORKSPACE", "Drawer photoUri changed: $uri")
            headerProfileImage?.let { img ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(img)
            }
        }

        // Click listeners
        optionProfileEdit?.setOnClickListener {
            // Navigate ke ProfileEditFragment kalau ada
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        optionAccount?.setOnClickListener {
            val container = accountOptionsContainer ?: return@setOnClickListener
            container.visibility = when (container.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        optionLogout?.setOnClickListener {
            auth.signOut()
            finish() // Close workspace activity
        }
    }

    private fun loadInitialProfile() {
        auth.currentUser?.let { user ->
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DBG_WORKSPACE", "User reloaded: ${user.displayName}, ${user.photoUrl}")
                    if (sharedViewModel.displayName.value.isNullOrEmpty()) {
                        sharedViewModel.displayName.value = user.displayName
                    }
                    if (sharedViewModel.photoUri.value == null && user.photoUrl != null) {
                        sharedViewModel.photoUri.value = user.photoUrl
                    }
                } else {
                    Log.w("DBG_WORKSPACE", "User reload failed", task.exception)
                }
            }
        }
    }
}