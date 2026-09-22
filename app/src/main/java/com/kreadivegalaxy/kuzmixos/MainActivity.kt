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
            val hasRecordAudio = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasRecordAudio) {
                android.util.Log.d("KuzmixOS", "KuzmixVoiceService deferred: RECORD_AUDIO permission not yet granted.")
                return
            }
            val voiceServiceIntent = android.content.Intent(this, KuzmixVoiceService::class.java).apply {
                putExtra("EXTRA_START_LISTENING", true)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(voiceServiceIntent)
            } else {
                startService(voiceServiceIntent)
            }
        } catch (unused: Throwable) {
            android.util.Log.e("KuzmixOS", "Failed to start KuzmixVoiceService: ${unused.message}")
        }
    }

    override fun onResume() {
        super.onResume()
    }

    @Deprecated("Use Activity Result API instead")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        @Suppress("DEPRECATION")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2026) {
            android.util.Log.d("KuzmixOS", "Runtime permissions response received.")
        }
    }

    @Deprecated("Use Activity Result API instead")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2027) {
            startKuzmixVoiceService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request required permissions at runtime smoothly without blocking UI
        try {
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
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                permissionsList.add("android.permission.POST_NOTIFICATIONS")
                permissionsList.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                permissionsList.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                permissionsList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            requestPermissions(permissionsList.toTypedArray(), 2026)
        } catch (e: Throwable) {
            android.util.Log.w("KuzmixOS", "Permission request non-fatal error: ${e.message}")
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

        // Ensure icon resolver uses persisted style
        try {
            val savedStyle = IconStylePreferences.getSelectedStyle(this)
            IconResolver.setCurrentStyle(savedStyle)
        } catch (e: Throwable) {
            android.util.Log.e("KuzmixOS", "Icon resolver init failed: ${e.message}")
        }

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
            
            val isInitiallyAuthenticated = remember {
                try {
                    sharedPrefs?.getBoolean("is_logged_in", false) == true ||
                    sharedPrefSettings?.getBoolean("is_authenticated", false) == true
                } catch (_: Throwable) {
                    false
                }
            }
            var isUserLoggedIn by remember {
                mutableStateOf(isInitiallyAuthenticated)
            }
            var themeMode by remember {
                mutableStateOf(sharedPrefSettings?.getString("theme_mode", "system") ?: "system")
            }

            androidx.activity.compose.BackHandler(enabled = true) {
                // Consume back press at root level so the launcher OS never closes to desktop/black screen
            }

            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> systemDark
            }

            androidx.compose.runtime.LaunchedEffect(isUserLoggedIn) {
                if (isUserLoggedIn) {
                    startKuzmixVoiceService()
                    try {
                        val serviceIntent = android.content.Intent(context, KuzmixLauncherService::class.java)
                        context.startService(serviceIntent)
                    } catch (unused: Throwable) {
                        android.util.Log.e("KuzmixOS", "Failed to start KuzmixLauncherService: ${unused.message}")
                    }
                }
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                if (!isUserLoggedIn) {
                    BootSequenceScreen(
                        hardwareTier = hardwareTier,
                        deviceModel = deviceModel
                    ) {
                        try {
                            sharedPrefs?.edit()?.putBoolean("is_logged_in", true)?.apply()
                            sharedPrefSettings?.edit()?.putBoolean("is_authenticated", true)?.apply()
                        } catch (_: Throwable) {}
                        isUserLoggedIn = true
                        startKuzmixVoiceService()
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
