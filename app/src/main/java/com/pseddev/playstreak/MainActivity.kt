package com.pseddev.mystreak

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.pseddev.mystreak.databinding.ActivityMainBinding
import com.pseddev.mystreak.ui.sync.SyncDialogFragment
import com.pseddev.mystreak.utils.GoogleDriveHelper
import com.pseddev.mystreak.utils.SyncManager
import com.pseddev.mystreak.utils.ConfigurationManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val configurationManager = ConfigurationManager.getInstance(this)
        val nightMode = if (configurationManager.isDarkModeEnabled()) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        freezePastCalendarDays()

        // Check if we should show sync dialog after a brief delay
        lifecycleScope.launch {
            delay(1000) // Wait for UI to settle
            checkAndShowSyncDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        freezePastCalendarDays()
    }

    private fun freezePastCalendarDays() {
        lifecycleScope.launch {
            (application as MyStreakApplication).repository.freezePastCalendarDaysIfNeeded()
        }
    }

    private fun checkAndShowSyncDialog() {
        val app = application as MyStreakApplication
        val driveHelper = GoogleDriveHelper(this)
        val syncManager = SyncManager(this, app.repository, driveHelper)

        // Check if we should show the sync dialog
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null && syncManager.isSyncEnabled()) {
            val lastSyncTime = syncManager.getLastSyncTime()
            val currentTime = System.currentTimeMillis()
            val twentyFourHoursInMillis = 24 * 60 * 60 * 1000

            if (lastSyncTime == 0L || (currentTime - lastSyncTime) > twentyFourHoursInMillis) {
                // Show sync dialog
                val syncDialog = SyncDialogFragment()
                syncDialog.show(supportFragmentManager, "SyncDialog")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, "Settings")
            .setIcon(R.drawable.ic_settings)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == MENU_SETTINGS) {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            if (navController.currentDestination?.id != R.id.mainFragment) {
                navController.navigate(R.id.mainFragment)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private companion object {
        const val MENU_SETTINGS = 1001
    }
}
