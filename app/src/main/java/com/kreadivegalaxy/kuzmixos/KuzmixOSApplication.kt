package com.kreadivegalaxy.kuzmixos

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.kreadivegalaxy.kuzmixos.diagnostics.KuzmixCrashLoggingService
class KuzmixOSApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        Log.i("KuzmixOS", "KuzmixOS initialized in 100% local offline mode.")

        // Initialize local Room crash logging service
        try {
            KuzmixCrashLoggingService.initialize(this)
        } catch (e: Throwable) {
            Log.e("KuzmixOS", "Failed to initialize KuzmixCrashLoggingService: ${e.message}", e)
        }

        // Initialize icon style from persisted preferences
        try {
            val savedStyle = IconStylePreferences.getSelectedStyle(this)
            IconResolver.setCurrentStyle(savedStyle)
            Log.i("KuzmixOS", "Icon style initialized: ${savedStyle.id}")
        } catch (e: Throwable) {
            Log.e("KuzmixOS", "Failed to initialize icon style: ${e.message}", e)
        }

        try {
            WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: Throwable) {
            Log.w("KuzmixOS", "WorkManager init handled: ${e.message}")
        }

        try {
            KuzmixServiceKeeper.schedule(this)
        } catch (e: Throwable) {
Log.w("KuzmixOS", "ServiceKeeper schedule failed: ${e.message}")
        }
    }

override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
