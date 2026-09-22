package com.kreadivegalaxy.kuzmixos.diagnostics

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AppCrashLog::class], version = 1, exportSchema = false)
abstract class KuzmixDiagnosticDatabase : RoomDatabase() {
    abstract fun crashLogDao(): CrashLogDao

    companion object {
        @Volatile
        private var INSTANCE: KuzmixDiagnosticDatabase? = null

        fun getInstance(context: Context): KuzmixDiagnosticDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KuzmixDiagnosticDatabase::class.java,
                    "kuzmix_diagnostics.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
