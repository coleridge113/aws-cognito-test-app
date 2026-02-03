package com.example.aws_cognito_test.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class IotLocalDataSource(private val dataStore: DataStore<Preferences>) {
    private val CERT_PEM = stringPreferencesKey("IOT_CERT_PEM")
    private val THING_NAME = stringPreferencesKey("IOT_THING_NAME")

    val iotIdentityFlow: Flow<IotIdentity> = dataStore.data.map { prefs ->
        IotIdentity(
            thingName = prefs[THING_NAME],
            certPem = prefs[CERT_PEM]
        )
    }

    suspend fun saveIotIdentity(thingName: String, certPem: String) {
        dataStore.edit { prefs ->
            prefs[THING_NAME] = thingName
            prefs[CERT_PEM] = certPem
        }
    }
}

data class IotIdentity(
    val thingName: String?,
    val certPem: String?
)
