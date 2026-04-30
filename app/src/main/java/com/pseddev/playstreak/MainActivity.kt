package com.pseddev.playstreak

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.pseddev.playstreak.databinding.ActivityMainBinding
import com.pseddev.playstreak.ui.sync.SyncDialogFragment
import com.pseddev.playstreak.utils.GoogleDriveHelper
import com.pseddev.playstreak.utils.SyncManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Check if we should show sync dialog after a brief delay
        lifecycleScope.launch {
            delay(1000) // Wait for UI to settle
            checkAndShowSyncDialog()
        }
    }
    
    private fun checkAndShowSyncDialog() {
        val app = application as PlayStreakApplication
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
}
