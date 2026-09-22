package com.kreadivegalaxy.kuzmixos

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class KuzmixLauncherService : Service() {

    override fun onCreate() {
        super.onCreate()
        
        // Start foreground to ensure OS keeps it running persistently
        try {
            createNotificationChannel()
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    startForeground(1337, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } catch (e: Exception) {
                    android.util.Log.e("KuzmixOS", "Failed to start dataSync FGS in onCreate: ${e.message}")
                }
            } else {
                startForeground(1337, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("KuzmixOS", "KuzmixLauncherService startForeground failed: ${e.message}")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            createNotificationChannel()
            val notification = createNotification()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    startForeground(1337, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } catch (e: Exception) {
                    android.util.Log.e("KuzmixOS", "Failed to start dataSync FGS in onStartCommand: ${e.message}")
                }
            } else {
                startForeground(1337, notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("KuzmixOS", "KuzmixLauncherService onStartCommand startForeground failed: ${e.message}")
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        android.util.Log.d("KuzmixOS", "KuzmixLauncherService onTaskRemoved - scheduling restart")
        try {
            val restartIntent = Intent(applicationContext, KuzmixLauncherService::class.java)
            val pendingIntent = android.app.PendingIntent.getService(
                applicationContext,
                1337,
                restartIntent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            alarmManager?.set(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + 2000,
                pendingIntent
            )
        } catch (e: Throwable) {
android.util.Log.e("KuzmixOS", "Failed to schedule LauncherService restart: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Kuzmix OS Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Kuzmix Sentinel Active")
            .setContentText("Monitoring Kuzmix OS background neural processes.")
            .setSmallIcon(R.drawable.ic_kuzmix_notification)
.setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "kuzmix_sentinel_channel"
    }
}
