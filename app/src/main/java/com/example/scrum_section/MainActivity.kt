package com.example.scrum_section

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.stratify.view.user.ProfileOptionsFragment
import com.example.stratify.R
import com.example.stratify.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), ProfileOptionsFragment.OnOptionSelectedListener {

    private lateinit var binding: ActivityMainBinding // View binding for the activity's layout
    private lateinit var navController: NavController // NavController to manage app navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and set the content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the NavHostFragment and NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up the bottom navigation view with the NavController
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            // This ensures that clicking a bottom navigation item navigates to the correct destination.
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        // This listener ensures that the correct bottom navigation item is selected when the destination changes.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val menu = binding.bottomNavigation.menu
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                // Checks if the menu item's ID matches the destination's ID.
                if (item.itemId == destination.id) {
                    item.isChecked = true
                }
            }
        }
    }

    /**
     * Callback from ProfileOptionsFragment when "Edit Profile" is selected.
     */
    override fun onProfileEditSelected() {
        navController.navigate(R.id.profileEditFragment)
    }

    /**
     * Callback from ProfileOptionsFragment when "Logout" is selected.
     */
    override fun onLogoutSelected() {
        TODO("Not yet implemented")
    }

    /**
     * Callback from the language selection fragment.
     */
    override fun onEnglishSelected() {
        // TODO: Implement language change logic
    }

    /**
     * Callback from the language selection fragment.
     */
    override fun onIndonesianSelected() {
        // TODO: Implement language change logic
    }

    /**
     * Callback from ProfileOptionsFragment when "Account" is selected.
     */
    override fun onAccountSelected() {
        TODO("Not yet implemented")
    }

    /**
     * Callback from ProfileOptionsFragment when "Language" is selected.
     */
    override fun onLanguageSelected() {
        TODO("Not yet implemented")
    }
}
