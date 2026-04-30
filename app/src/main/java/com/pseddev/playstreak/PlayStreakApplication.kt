package com.pseddev.playstreak

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pseddev.playstreak.data.AppDatabase
import com.pseddev.playstreak.data.repository.PianoRepository

class PlayStreakApplication : Application() {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        PianoRepository(
            database.pieceOrTechniqueDao(),
            database.activityDao(),
            database.dailyCalendarStateDao(),
            database.achievementDao(),
            this
        ) 
    }
    
    // Firebase Analytics instance - accessible globally
    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set
    
    // Firebase Crashlytics instance - accessible globally
    lateinit var firebaseCrashlytics: FirebaseCrashlytics
        private set
    
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            return
        }

        // Initialize Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
        
        // Initialize Firebase Crashlytics
        firebaseCrashlytics = FirebaseCrashlytics.getInstance()
        firebaseCrashlytics.setCrashlyticsCollectionEnabled(true)
    }
}
