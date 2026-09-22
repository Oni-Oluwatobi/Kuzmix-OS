package com.kreadivegalaxy.kuzmixos.diagnostics

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

object KuzmixCrashLoggingService {
    private const val TAG = "KuzmixCrashLogging"
    private const val PREFS_NAME = "kuzmix_crash_prefs"
    private const val KEY_HAS_UNHANDLED_CRASH = "has_unhandled_crash"
    private var repository: CrashLogRepository? = null

    fun initialize(application: Application) {
        repository = CrashLogRepository(application)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)

                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))

                val log = AppCrashLog(
                    timestamp = System.currentTimeMillis(),
                    exceptionClass = throwable.javaClass.name,
                    message = throwable.message ?: "No message",
                    stackTrace = sw.toString(),
                    deviceModel = Build.MODEL ?: "Unknown",
                    androidVersion = Build.VERSION.RELEASE ?: "Unknown",
                    appVersion = try {
                        application.packageManager.getPackageInfo(application.packageName, 0).versionName ?: "Unknown"
                    } catch (e: Exception) {
                        "Unknown"
                    },
                    isHandled = false
                )

                val prefs = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)
                prefs.edit().putBoolean(KEY_HAS_UNHANDLED_CRASH, true).apply()

                try {
                    val repo = CrashLogRepository(application)
                    kotlinx.coroutines.runBlocking {
                        repo.insertCrash(log)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist crash log: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log crash: ${e.message}")
            }

            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.i(TAG, "Crash logging service initialized.")
    }

    fun hasUnhandledCrashOccurred(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)
            prefs.getBoolean(KEY_HAS_UNHANDLED_CRASH, false)
        } catch (e: Exception) {
            false
        }
    }

    fun markCrashAsHandled(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_HAS_UNHANDLED_CRASH, false).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to mark crash as handled: ${e.message}")
        }
    }
}
