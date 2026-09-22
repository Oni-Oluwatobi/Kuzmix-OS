package com.kreadivegalaxy.kuzmixos.diagnostics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crash_logs")
data class AppCrashLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val exceptionClass: String = "",
    val message: String = "",
    val stackTrace: String = "",
    val deviceModel: String = "",
    val androidVersion: String = "",
    val appVersion: String = "",
    val isHandled: Boolean = false
)
