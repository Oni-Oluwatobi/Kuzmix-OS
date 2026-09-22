package com.kreadivegalaxy.kuzmixos

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interface contract for authentication operations in Kuzmix OS.
 */
interface AuthRepository {
    suspend fun signIn(username: String, passcode: String): Result<Boolean>
    suspend fun signInAsGuest(): Result<Boolean>
    suspend fun signInWithBiometrics(): Result<Boolean>
    fun isAuthenticated(): Boolean
}

/**
 * Concrete repository implementation with safe error handling and constructor dependency injection.
 */
class KuzmixAuthRepository(
    private val context: Context
) : AuthRepository {

    companion object {
        private const val TAG = "KuzmixAuthRepo"
        private const val PREFS_NAME_LEGACY = "kuzmix_prefs"
        private const val PREFS_NAME_SETTINGS = "KuzmixSettings"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_AUTH_USER = "auth_username"
        private const val KEY_AUTH_METHOD = "auth_method"
    }

    private fun getLegacyPrefs(): SharedPreferences? {
        return try {
            context.getSharedPreferences(PREFS_NAME_LEGACY, Context.MODE_PRIVATE)
        } catch (t: Throwable) {
            Log.e(TAG, "Error accessing legacy shared preferences", t)
            null
        }
    }

    private fun getSettingsPrefs(): SharedPreferences? {
        return try {
            context.getSharedPreferences(PREFS_NAME_SETTINGS, Context.MODE_PRIVATE)
        } catch (t: Throwable) {
            Log.e(TAG, "Error accessing settings shared preferences", t)
            null
        }
    }

    override fun isAuthenticated(): Boolean {
        return try {
            val legacyAuth = getLegacyPrefs()?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
            val settingsAuth = getSettingsPrefs()?.getBoolean(KEY_IS_AUTHENTICATED, false) ?: false
            legacyAuth || settingsAuth
        } catch (t: Throwable) {
            Log.e(TAG, "Error checking authentication status", t)
            false
        }
    }

    private fun persistSession(username: String, method: String): Boolean {
        var success = false
        try {
            getLegacyPrefs()?.edit()
                ?.putBoolean(KEY_IS_LOGGED_IN, true)
                ?.putString(KEY_AUTH_USER, username)
                ?.putString(KEY_AUTH_METHOD, method)
                ?.apply()
            success = true
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to persist to legacy preferences", t)
        }

        try {
            getSettingsPrefs()?.edit()
                ?.putBoolean(KEY_IS_AUTHENTICATED, true)
                ?.putString(KEY_AUTH_USER, username)
                ?.apply()
            success = true
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to persist to settings preferences", t)
        }

        return success
    }

    override suspend fun signIn(username: String, passcode: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val trimmedUser = username.trim()
            val trimmedPass = passcode.trim()

            // Validate requirements
            if (trimmedUser.isNotEmpty() && trimmedPass.length < 4) {
                return@withContext Result.failure(IllegalArgumentException("Passcode must be at least 4 characters."))
            }

            val effectiveUser = if (trimmedUser.isNotEmpty()) trimmedUser else "Kuzmix Operator"
            val persisted = persistSession(effectiveUser, "credentials")
            if (persisted) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Failed to securely write session to device storage."))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unhandled exception during signIn", t)
            Result.failure(t)
        }
    }

    override suspend fun signInAsGuest(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val persisted = persistSession("Guest Operator", "guest")
            if (persisted) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Failed to persist guest session."))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unhandled exception during guest signIn", t)
            Result.failure(t)
        }
    }

    override suspend fun signInWithBiometrics(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val persisted = persistSession("Biometric Verified", "biometric")
            if (persisted) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Failed to persist biometric session."))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unhandled exception during biometric signIn", t)
            Result.failure(t)
        }
    }
}
