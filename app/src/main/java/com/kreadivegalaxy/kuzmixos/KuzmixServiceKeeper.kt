package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.work.*
import java.util.concurrent.TimeUnit

class KuzmixServiceKeeperWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("kuzmix_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("is_logged_in", false)) {
            return Result.success()
        }
        ensureVoiceServiceRunning()
        ensureLauncherServiceRunning()
        return Result.success()
    }

    private fun ensureVoiceServiceRunning() {
        try {
            val intent = Intent(applicationContext, KuzmixVoiceService::class.java).apply {
                putExtra("EXTRA_START_LISTENING", true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    applicationContext.startForegroundService(intent)
                } catch (e: Throwable) {
                    android.util.Log.w("KuzmixOS", "ServiceKeeper: Background FGS deferred: ${e.message}")
                }
            } else {
                applicationContext.startService(intent)
            }
        } catch (e: Throwable) {
            android.util.Log.e("KuzmixOS", "ServiceKeeper: Failed to ensure VoiceService: ${e.message}")
        }
    }

    private fun ensureLauncherServiceRunning() {
        try {
            val intent = Intent(applicationContext, KuzmixLauncherService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    applicationContext.startForegroundService(intent)
                } catch (e: Throwable) {
                    android.util.Log.w("KuzmixOS", "ServiceKeeper: Background FGS deferred: ${e.message}")
                }
            } else {
                applicationContext.startService(intent)
            }
        } catch (e: Throwable) {
            android.util.Log.e("KuzmixOS", "ServiceKeeper: Failed to ensure LauncherService: ${e.message}")
        }
    }
}

object KuzmixServiceKeeper {

    private const val WORK_NAME = "kuzmix_service_keeper"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<KuzmixServiceKeeperWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        android.util.Log.d("KuzmixOS", "ServiceKeeper: Periodic work scheduled (every 15 min)")
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
