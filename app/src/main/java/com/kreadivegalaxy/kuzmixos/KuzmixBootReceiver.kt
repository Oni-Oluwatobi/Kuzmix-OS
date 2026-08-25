package com.kreadivegalaxy.kuzmixos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class KuzmixBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                val voiceIntent = Intent(context, KuzmixVoiceService::class.java).apply {
                    putExtra("EXTRA_START_LISTENING", true)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(voiceIntent)
                } else {
                    context.startService(voiceIntent)
                }
            } catch (e: Exception) {
                android.util.Log.e("KuzmixOS", "Failed to start KuzmixVoiceService on boot: ${e.message}")
            }

            try {
                val launcherIntent = Intent(context, KuzmixLauncherService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(launcherIntent)
                } else {
                    context.startService(launcherIntent)
                }
            } catch (e: Exception) {
                android.util.Log.e("KuzmixOS", "Failed to start KuzmixLauncherService on boot: ${e.message}")
            }
        }
    }
}
