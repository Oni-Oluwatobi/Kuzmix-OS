package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Immutable UI State for the Kuzmix OS Sign-In Screen.
 */
data class LoginUiState(
    val username: String = "",
    val passcode: String = "",
    val isAuthenticating: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

/**
 * ViewModel managing authentication state and actions for KuzmixLoginScreen.
 * Injects [AuthRepository] via constructor injection.
 */
class KuzmixLoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "KuzmixLoginVM"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        // Safe state initialization
        try {
            val alreadyAuth = authRepository.isAuthenticated()
            _uiState.update { it.copy(isAuthenticated = alreadyAuth) }
        } catch (t: Throwable) {
            Log.e(TAG, "Error checking initial authentication state in ViewModel", t)
            _uiState.update { it.copy(isAuthenticated = false) }
        }
    }

    fun onUsernameChanged(newUsername: String) {
        _uiState.update { it.copy(username = newUsername, errorMessage = null) }
    }

    fun onPasscodeChanged(newPasscode: String) {
        _uiState.update { it.copy(passcode = newPasscode, errorMessage = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun signInWithCredentials(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        if (currentState.isAuthenticating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            try {
                val result = authRepository.signIn(currentState.username, currentState.passcode)
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isAuthenticating = false, isAuthenticated = true, errorMessage = null) }
                        onSuccess()
                    },
                    onFailure = { error ->
                        Log.w(TAG, "Sign-in credentials rejected: ${error.message}")
                        _uiState.update {
                            it.copy(
                                isAuthenticating = false,
                                errorMessage = error.message ?: "Authentication failed. Please verify your credentials."
                            )
                        }
                    }
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Unhandled exception during signInWithCredentials", t)
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = "An unexpected error occurred during sign-in."
                    )
                }
            }
        }
    }

    fun signInAsGuest(onSuccess: () -> Unit) {
        if (_uiState.value.isAuthenticating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            try {
                val result = authRepository.signInAsGuest()
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isAuthenticating = false, isAuthenticated = true, errorMessage = null) }
                        onSuccess()
                    },
                    onFailure = { error ->
                        Log.w(TAG, "Guest access failed: ${error.message}")
                        _uiState.update {
                            it.copy(
                                isAuthenticating = false,
                                errorMessage = error.message ?: "Failed to enter as Guest."
                            )
                        }
                    }
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Unhandled exception during signInAsGuest", t)
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = "An unexpected error occurred entering Guest mode."
                    )
                }
            }
        }
    }

    fun signInWithBiometrics(onSuccess: () -> Unit) {
        if (_uiState.value.isAuthenticating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true, errorMessage = null) }
            try {
                val result = authRepository.signInWithBiometrics()
                result.fold(
                    onSuccess = {
                        _uiState.update { it.copy(isAuthenticating = false, isAuthenticated = true, errorMessage = null) }
                        onSuccess()
                    },
                    onFailure = { error ->
                        Log.w(TAG, "Biometric login failed: ${error.message}")
                        _uiState.update {
                            it.copy(
                                isAuthenticating = false,
                                errorMessage = error.message ?: "Biometric verification unsuccessful."
                            )
                        }
                    }
                )
            } catch (t: Throwable) {
                Log.e(TAG, "Unhandled exception during signInWithBiometrics", t)
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        errorMessage = "An unexpected error occurred during biometric sign-in."
                    )
                }
            }
        }
    }

    /**
     * Factory class for instantiating [KuzmixLoginViewModel] with dependency injection.
     */
    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(KuzmixLoginViewModel::class.java)) {
                val repo = KuzmixAuthRepository(appContext)
                return KuzmixLoginViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
