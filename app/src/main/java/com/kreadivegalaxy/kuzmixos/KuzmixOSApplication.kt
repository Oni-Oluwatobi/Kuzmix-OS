package com.kreadivegalaxy.kuzmixos

import android.app.Application
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

class KuzmixOSApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        Log.i("KuzmixOS", "KuzmixOS initialized in 100% local offline mode.")
        requestBatteryOptimizationBypass()
        try {
            KuzmixServiceKeeper.schedule(this)
        } catch (e: Exception) {
            Log.w("KuzmixOS", "ServiceKeeper schedule failed: ${e.message}")
        }
    }

    private fun requestBatteryOptimizationBypass() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(POWER_SERVICE) as PowerManager
                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    Log.d("KuzmixOS", "Device is restricting battery - requesting exemption")
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = android.net.Uri.parse("package:$packageName")
                        }
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.w("KuzmixOS", "Failed to request battery optimization exemption: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("KuzmixOS", "Battery optimization check failed: ${e.message}")
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
