package com.example.aws_cognito_test.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AuthLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {
    private val JWT_TOKEN = stringPreferencesKey("JWT_TOKEN")

    val jwtTokenFlow: Flow<String?> = dataStore.data.map { prefs -> prefs[JWT_TOKEN] }

    suspend fun saveJwtToken(token: String) {
        dataStore.edit { prefs ->
            prefs[JWT_TOKEN] = token
        }
    }

    suspend fun getJwtToken(): String? {
        return dataStore.data.map { prefs ->
            prefs[JWT_TOKEN]
        }.firstOrNull()
    }

    suspend fun clearJwtToken() {
        dataStore.edit { prefs ->
            prefs.remove(JWT_TOKEN)
        }
    }
}
