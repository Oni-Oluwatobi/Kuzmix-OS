/* 
 * KC Studio - Professional Mobile IDE 
 * Developed by: ONI OLUWATOBI 
 * Copyright 2026 THE KREADIVE GALAXY 
 */
package com.kreadivegalaxy.kuzmixos

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import com.kreadivegalaxy.kuzmixos.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_CODE_SET_DEFAULT = 1337
    }

    private fun promoteToDefaultLauncher(activity: android.app.Activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(ROLE_SERVICE) as android.app.role.RoleManager
            if (roleManager.isRoleAvailable(android.app.role.RoleManager.ROLE_HOME) && 
                !roleManager.isRoleHeld(android.app.role.RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(android.app.role.RoleManager.ROLE_HOME)
                @Suppress("DEPRECATION")
                activity.startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT)
            }
        } else {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_HOME)
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            activity.startActivity(intent)
        }
    }

    private fun startKuzmixVoiceService() {
        try {
            val voiceServiceIntent = android.content.Intent(this, KuzmixVoiceService::class.java).apply {
                putExtra("EXTRA_START_LISTENING", true)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(voiceServiceIntent)
            } else {
                startService(voiceServiceIntent)
            }
        } catch (unused: Exception) {
            android.util.Log.e("KuzmixOS", "Failed to start KuzmixVoiceService: ${unused.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        startKuzmixVoiceService()
    }

    @Deprecated("Use Activity Result API instead")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        @Suppress("DEPRECATION")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2026) {
            // Permissions granted, restart voice service to ensure it has proper permissions
            startKuzmixVoiceService()
        }
    }

    @Deprecated("Use Activity Result API instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2027) {
            // Overlay permission result, restart voice service to enable overlay
            startKuzmixVoiceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request SYSTEM_ALERT_WINDOW permission for overlay (critical for voice assistant UI)
        if (!android.provider.Settings.canDrawOverlays(this)) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            @Suppress("DEPRECATION")
            startActivityForResult(intent, 2027)
        }

        // Request required permissions at runtime (Contacts, Phone, Mic, Calendar, Location, Camera, Media/Storage, Notifications)
        val permissionsList = mutableListOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.READ_CALENDAR,
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA
        )
        if (android.os.Build.VERSION.SDK_INT >= 33) { // Build.VERSION_CODES.TIRAMISU
            permissionsList.add("android.permission.POST_NOTIFICATIONS")
            permissionsList.add(android.Manifest.permission.READ_MEDIA_IMAGES)
            permissionsList.add(android.Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionsList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissions(permissionsList.toTypedArray(), 2026)
        
        // Start services on startup
        startKuzmixVoiceService()
        
        // Offload launcher service to background scope
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val serviceIntent = android.content.Intent(this@MainActivity, KuzmixLauncherService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } catch (unused: Exception) {
                android.util.Log.e("KuzmixOS", "Failed to start KuzmixLauncherService: ${unused.message}")
            }
        }
        
        val hardwareTier = try {
            HardwareDetection.detectTier(this)
        } catch (e: Throwable) {
            android.util.Log.e("KuzmixOS", "HardwareDetection.detectTier failed: ${e.message}", e)
            HardwareTier.NOVA
        }
        val deviceModel = try {
            HardwareDetection.getDeviceModel()
        } catch (e: Throwable) {
            android.util.Log.e("KuzmixOS", "HardwareDetection.getDeviceModel failed: ${e.message}", e)
            android.os.Build.MODEL ?: "Unknown"
        }
        val appLoader = AppLoader(this)

        setContent {
            val context = LocalContext.current

            val sharedPrefs = remember {
                try {
                    context.getSharedPreferences("kuzmix_prefs", Context.MODE_PRIVATE)
                } catch (unused: Exception) {
                    null
                }
            }
            val sharedPrefSettings = remember {
                try {
                    context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
                } catch (unused: Exception) {
                    null
                }
            }
            
            // To ensure the secure login/lock screen is always presented on fresh install, updates,
            // or cold starts (but not on simple home key presses when already unlocked),
            // we default isUserLoggedIn to false on cold launch.
            var isUserLoggedIn by remember {
                mutableStateOf(value = false)
            }
            var themeMode by remember {
                mutableStateOf(sharedPrefSettings?.getString("theme_mode", "system") ?: "system")
            }

            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            androidx.compose.runtime.LaunchedEffect(isUserLoggedIn) {
                if (isUserLoggedIn) {
                    val voiceServiceIntent = android.content.Intent(context, KuzmixVoiceService::class.java).apply {
                        putExtra("EXTRA_START_LISTENING", true)
                    }
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(voiceServiceIntent)
                        } else {
                            context.startService(voiceServiceIntent)
                        }
                    } catch (unused: Exception) {
                        android.util.Log.e("KuzmixOS", "Failed to start KuzmixVoiceService on login state: ${unused.message}")
                    }
                }
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                if (!isUserLoggedIn) {
                    BootSequenceScreen(
                        hardwareTier = hardwareTier,
                        deviceModel = deviceModel
                    ) {
                        isUserLoggedIn = true
                        promoteToDefaultLauncher(this@MainActivity)
                        
                        // Immediately start voice service after setup completes
                        try {
                            val voiceServiceIntent = android.content.Intent(this@MainActivity, KuzmixVoiceService::class.java).apply {
                                putExtra("EXTRA_START_LISTENING", true)
                            }
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                startForegroundService(voiceServiceIntent)
                            } else {
                                startService(voiceServiceIntent)
                            }
                        } catch (unused: Exception) {
                            android.util.Log.e("KuzmixOS", "Failed to start voice service after setup: ${unused.message}")
                        }
                    }
                } else {
                    HomeDashboard(
                        hardwareTier = hardwareTier,
                        appLoader = appLoader,
                        themeMode = themeMode,
                        onThemeModeChange = { newMode ->
                            themeMode = newMode
                            try {
                                val sharedPrefSettings = context.getSharedPreferences("KuzmixSettings", Context.MODE_PRIVATE)
                                sharedPrefSettings.edit().putString("theme_mode", newMode).apply()
                            } catch (e: Exception) {
                                // Ignore
                            }
                        },
                        onLockOS = {
                            isUserLoggedIn = false
                            try {
                                sharedPrefs?.edit()?.putBoolean("is_logged_in", false)?.apply()
                                sharedPrefSettings?.edit()?.putBoolean("is_authenticated", false)?.apply()
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    )
                }
            }
        }
    }
}

class NeoLauncherActivity : MainActivity()
