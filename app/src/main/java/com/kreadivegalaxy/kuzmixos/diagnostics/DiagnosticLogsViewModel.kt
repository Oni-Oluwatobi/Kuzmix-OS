package com.kreadivegalaxy.kuzmixos.diagnostics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DiagnosticLogsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CrashLogRepository(application)

    private val _logs = MutableStateFlow<List<AppCrashLog>>(emptyList())
    val logs: StateFlow<List<AppCrashLog>> = _logs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadLogs()
    }

    fun loadLogs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _logs.value = repository.getAllCrashLogs()
            } catch (e: Exception) {
                _logs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAllAsHandled() {
        viewModelScope.launch {
            try {
                val currentLogs = _logs.value.filter { !it.isHandled }
                for (log in currentLogs) {
                    repository.markAsHandled(log.id)
                }
                KuzmixCrashLoggingService.markCrashAsHandled(getApplication())
                loadLogs()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch {
            try {
                repository.clearAllLogs()
                loadLogs()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
