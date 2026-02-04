package com.example.aws_cognito_test.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class IotLocalDataSource(private val dataStore: DataStore<Preferences>) {
    private val CERT_PEM = stringPreferencesKey("IOT_CERT_PEM")
    private val THING_NAME = stringPreferencesKey("IOT_THING_NAME")

    val iotIdentityFlow: Flow<IotIdentityDto> = dataStore.data.map { prefs ->
        IotIdentityDto(
            thingName = prefs[THING_NAME],
            certPem = prefs[CERT_PEM]
        )
    }

    suspend fun saveIotIdentity(identity: IotIdentityDto) {
        dataStore.edit { prefs ->
            prefs[THING_NAME] = identity.thingName!!
            prefs[CERT_PEM] = identity.certPem!!
        }
    }

    suspend fun fetchIotIdentity(): IotIdentityDto? {
        return dataStore.data
        .map { prefs ->
            val thingName = prefs[THING_NAME]
            val certPem = prefs[CERT_PEM]

            if (thingName != null && certPem != null) {
                IotIdentityDto(thingName = thingName, certPem = certPem)
            } else {
                null
            }
        }
        .firstOrNull()
    }
}

data class IotIdentityDto(
    val thingName: String?,
    val certPem: String?
)
