package com.example.stratify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope // <-- Tetap perlukan ini
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.stratify.databinding.ActivityMainBinding
import com.example.stratify.view.profile.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import androidx.core.content.edit
import kotlinx.coroutines.launch // <-- Tetap perlukan ini

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Kita tetap butuh repository untuk setupBottomNavigation
    private val repository = WorkspaceRepository()

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "")
        val context = if (!language.isNullOrEmpty()) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = newBase.resources.configuration
            config.setLocale(locale)
            newBase.createConfigurationContext(config)
        } else newBase

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth.currentUser?.let { user ->
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DBG_MAIN", "User reloaded on start: ${user.displayName}, ${user.photoUrl}")
                    if (sharedViewModel.displayName.value.isNullOrEmpty()) {
                        sharedViewModel.displayName.value = user.displayName
                    }
                    if (sharedViewModel.photoUri.value == null && user.photoUrl != null) {
                        sharedViewModel.photoUri.value = user.photoUrl
                    }
                } else {
                    Log.w("DBG_MAIN", "User reload failed on start", task.exception)
                }
            }
        }

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
            R.id.navigation_scrum,
            R.id.workspaceListFragment
        )

        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
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
        binding.headerLayout.accountIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.END)
        }

        sharedViewModel.photoUri.observe(this) { uri ->
            Log.d("DBG_MAIN", "Header photoUri changed: $uri")
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.ic_acc)
                .circleCrop()
                .into(binding.headerLayout.accountIcon)
        }
    }

    private fun setupDrawer() {
        val headerView = binding.navViewProfile.getHeaderView(0) ?: return
        val headerProfileImage = headerView.findViewById<ImageView>(R.id.ivProfileImage)
        val headerName = headerView.findViewById<TextView>(R.id.tvProfileName)
        val headerEmail = headerView.findViewById<TextView>(R.id.tvProfileEmail)
        val optionProfileEdit = headerView.findViewById<TextView>(R.id.optionProfileEdit)
        val optionAccount = headerView.findViewById<TextView>(R.id.optionAccount)
        val accountOptionsContainer = headerView.findViewById<LinearLayout>(R.id.account_options_container)
        val optionLogout = headerView.findViewById<TextView>(R.id.optionLogout)
        val optionLanguage = headerView.findViewById<TextView>(R.id.optionLanguage)
        val languageOptionsContainer = headerView.findViewById<LinearLayout>(R.id.language_options_container)
        val optionEnglish = headerView.findViewById<TextView>(R.id.optionEnglish)
        val optionIndonesia = headerView.findViewById<TextView>(R.id.optionIndonesia)
        val currentUser = auth.currentUser
        if (sharedViewModel.displayName.value == null && !currentUser?.displayName.isNullOrEmpty()) {
            sharedViewModel.displayName.value = currentUser?.displayName
        }
        if (sharedViewModel.photoUri.value == null && currentUser?.photoUrl != null) {
            sharedViewModel.photoUri.value = currentUser.photoUrl
        }
        headerEmail?.text = currentUser?.email ?: "user.email@example.com"
        sharedViewModel.displayName.observe(this) { name ->
            Log.d("DBG_MAIN", "displayName observed in MainActivity: $name")
            headerName?.text = name ?: auth.currentUser?.displayName ?: "User Name"
        }
        sharedViewModel.photoUri.observe(this) { uri ->
            Log.d("DBG_MAIN", "photoUri observed in MainActivity: $uri")
            headerProfileImage?.let { img ->
                Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(img)
            }
        }
        optionProfileEdit?.setOnClickListener {
            navController.navigate(R.id.profileEditFragment)
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
        optionAccount?.setOnClickListener {
            val container = accountOptionsContainer ?: return@setOnClickListener
            container.visibility = when (container.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }
        optionLogout?.setOnClickListener {
            onLogoutSelected()
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
        optionLanguage?.setOnClickListener {
            val container = languageOptionsContainer ?: return@setOnClickListener
            container.visibility = when (container.visibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
        }
        // --- PERBAIKAN PANGGILAN FUNGSI BAHASA ---
        optionEnglish?.setOnClickListener {
            // Cukup panggil setLocale, recreate() sudah ada di dalamnya
            setLocale("en")
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        optionIndonesia?.setOnClickListener {
            // Cukup panggil setLocale, recreate() sudah ada di dalamnya
            setLocale("in")
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }
        // --- AKHIR PERBAIKAN ---
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (navController.currentDestination?.id != R.id.navigation_home) {
                        navController.navigate(R.id.navigation_home)
                    }
                    true
                }
                R.id.navigation_scrum -> {
                    if (navController.currentDestination?.id != R.id.navigation_scrum) {
                        navController.navigate(R.id.navigation_scrum)
                    }
                    true
                }
                R.id.navigation_workspace -> {
                    lifecycleScope.launch {
                        val hasWorkspaces = repository.hasWorkspaces().getOrElse { false }
                        val destinationId = if (hasWorkspaces) {
                            R.id.workspaceListFragment
                        } else {
                            R.id.navigation_workspace
                        }
                        if (navController.currentDestination?.id != destinationId) {
                            navController.navigate(destinationId)
                        }
                    }
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

    // --- PERBAIKAN FUNGSI setLocale ---
    private fun setLocale(languageCode: String) {
        // 1. Simpan pilihan bahasa ke SharedPreferences
        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        prefs.edit {
            putString("My_Lang", languageCode)
        }

        // 2. Update locale aplikasi saat ini
        val locale = Locale(languageCode)
        Locale.setDefault(locale) // Set default locale JVM
        val config = resources.configuration
        config.setLocale(locale) // Set locale untuk resources
        // Update konfigurasi resources Activity
        resources.updateConfiguration(config, resources.displayMetrics)

        // 3. Restart Activity
        recreate()
    }
    // --- AKHIR PERBAIKAN ---

    // Fungsi onEnglishSelected dan onIndonesianSelected tidak diperlukan lagi
    // karena pemanggilannya sudah langsung memanggil setLocale di setupDrawer

} // Akhir dari class MainActivity