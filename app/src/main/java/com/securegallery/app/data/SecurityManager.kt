package com.securegallery.app.data

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "security")

class SecurityManager(private val context: Context) {
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val pinEnabledKey = booleanPreferencesKey("pin_enabled")
    private val lockedKey = booleanPreferencesKey("locked")

    val isPinSet: Flow<Boolean> = context.dataStore.data.map { it[pinEnabledKey] ?: false }
    val isLocked: Flow<Boolean> = context.dataStore.data.map { it[lockedKey] ?: true }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[pinHashKey] = hashPin(pin)
            prefs[pinEnabledKey] = true
            prefs[lockedKey] = false
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val stored = context.dataStore.data.map { it[pinHashKey] }.first()
        return stored?.let { hashPin(pin) == it } ?: false
    }

    suspend fun unlock(pin: String): Boolean {
        if (!verifyPin(pin)) return false
        context.dataStore.edit { it[lockedKey] = false }
        return true
    }

    suspend fun lock() {
        context.dataStore.edit { it[lockedKey] = true }
    }

    suspend fun clearPin() {
        context.dataStore.edit {
            it.remove(pinHashKey)
            it[pinEnabledKey] = false
            it[lockedKey] = false
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}

