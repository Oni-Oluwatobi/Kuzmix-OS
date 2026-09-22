package com.kreadivegalaxy.kuzmixos.diagnostics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CrashLogDao {
    @Insert
    suspend fun insert(log: AppCrashLog): Long

    @Query("SELECT * FROM crash_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AppCrashLog>

    @Query("SELECT * FROM crash_logs WHERE isHandled = 0 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestUnhandledCrash(): AppCrashLog?

    @Query("UPDATE crash_logs SET isHandled = 1 WHERE id = :logId")
    suspend fun markAsHandled(logId: Long)

    @Query("DELETE FROM crash_logs")
    suspend fun clearAll()
}
