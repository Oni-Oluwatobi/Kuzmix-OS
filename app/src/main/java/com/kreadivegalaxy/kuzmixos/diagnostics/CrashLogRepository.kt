package com.kreadivegalaxy.kuzmixos.diagnostics

import android.content.Context

class CrashLogRepository(context: Context) {
    private val dao = KuzmixDiagnosticDatabase.getInstance(context).crashLogDao()

    suspend fun insertCrash(log: AppCrashLog): Long {
        return dao.insert(log)
    }

    suspend fun getAllCrashLogs(): List<AppCrashLog> {
        return dao.getAllLogs()
    }

    suspend fun getLatestUnhandledCrash(): AppCrashLog? {
        return dao.getLatestUnhandledCrash()
    }

    suspend fun markAsHandled(logId: Long) {
        dao.markAsHandled(logId)
    }

    suspend fun clearAllLogs() {
        dao.clearAll()
    }
}
