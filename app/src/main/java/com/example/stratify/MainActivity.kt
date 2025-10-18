package com.example.stratify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.stratify.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "")
        val context = if (!language.isNullOrEmpty()) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = newBase.resources.configuration
            config.setLocale(locale)
            // Use createConfigurationContext for correct context
            newBase.createConfigurationContext(config)
        } else newBase

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupHeader()
        setupBottomNavigation()
        setupDrawer()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val topLevelDestinations = setOf(
            R.id.navigation_home,
            R.id.navigation_workspace,
            R.id.navigation_scrum
        )

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Control visibility of the custom header based on the current destination
            binding.headerLayout.root.visibility = when (destination.id) {
                R.id.navigation_scrum,
                R.id.navigation_workspace,
                R.id.createWorkspaceFragment,
                R.id.joinWorkspaceFragment,
                R.id.workspaceListFragment,
                R.id.workspaceDetailFragment,
                R.id.fullAnalysisFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun setupHeader() {
        // Listener to open the drawer when the account icon in the custom header is clicked
        binding.headerLayout.accountIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun setupDrawer() {
        val headerView = binding.navViewProfile.getHeaderView(0)

        if (headerView == null) {
            return
        }

        // Find the views inside the header
        val optionProfileEdit = headerView.findViewById<TextView>(R.id.optionProfileEdit)
        val optionAccount = headerView.findViewById<TextView>(R.id.optionAccount)
        val accountOptionsContainer = headerView.findViewById<LinearLayout>(R.id.account_options_container)
        val optionLogout = headerView.findViewById<TextView>(R.id.optionLogout)

        val optionLanguage = headerView.findViewById<TextView>(R.id.optionLanguage)
        val languageOptionsContainer = headerView.findViewById<LinearLayout>(R.id.language_options_container)
        val optionEnglish = headerView.findViewById<TextView>(R.id.optionEnglish)
        val optionIndonesia = headerView.findViewById<TextView>(R.id.optionIndonesia)

        // --- Profile Edit Listener ---
        optionProfileEdit?.setOnClickListener {
            navController.navigate(R.id.profileEditFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        // --- Account Header Listener (Toggles visibility) ---
        optionAccount?.setOnClickListener {
            // Use Elvis operator for safety, though findViewById on a valid header should work
            val container = accountOptionsContainer ?: return@setOnClickListener
            container.visibility = when (container.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        // --- Logout Listener ---
        optionLogout?.setOnClickListener {
            onLogoutSelected()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        // --- Language Header Listener (Toggles visibility) ---
        optionLanguage?.setOnClickListener {
            // Use Elvis operator for safety
            val container = languageOptionsContainer ?: return@setOnClickListener
            container.visibility = when (container.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }

        // --- Language Selection Listeners ---
        optionEnglish?.setOnClickListener {
            onEnglishSelected()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        optionIndonesia?.setOnClickListener {
            onIndonesianSelected()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    private fun setupBottomNavigation() {
        // (This code remains correct for bottom navigation)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_home)
                        navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_scrum -> {
                    if (navController.currentDestination?.id != R.id.navigation_scrum)
                        navController.navigate(R.id.navigation_scrum)
                    true
                }
                R.id.navigation_workspace -> {
                    if (navController.currentDestination?.id != R.id.navigation_workspace)
                        navController.navigate(R.id.navigation_workspace)
                    true
                }
                else -> false
            }
        }
    }

    private fun onLogoutSelected() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun onEnglishSelected() {
        setLocale("en")
        recreate()
    }

    private fun onIndonesianSelected() {
        // Your Indonesian locale tag is "in" (standard)
        setLocale("in")
        recreate()
    }

    private fun setLocale(lang: String) {
        getSharedPreferences("Settings", Context.MODE_PRIVATE)
            .edit()
            .putString("My_Lang", lang)
            .apply()
    }
}