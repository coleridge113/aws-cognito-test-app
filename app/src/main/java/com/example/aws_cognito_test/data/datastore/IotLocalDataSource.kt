package com.example.aws_cognito_test.data.datastore

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.aws_cognito_test.data.utils.CryptoUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class IotLocalDataSource(private val dataStore: DataStore<Preferences>) {
    private val CERT_PEM = stringPreferencesKey("IOT_CERT_PEM")
    private val THING_NAME = stringPreferencesKey("IOT_THING_NAME")
    private val ENCRYPTED_KEY = stringPreferencesKey("ENCRYPTED_KEY")
    private val KEY_IV = stringPreferencesKey("KEY_IV")

    val iotIdentityFlow: Flow<IotIdentityDto> = dataStore.data.map { prefs ->
        IotIdentityDto(
            thingName = prefs[THING_NAME],
            certPem = prefs[CERT_PEM],
            iv = prefs[KEY_IV],
            encryptedKey = prefs[ENCRYPTED_KEY]
        )
   }

    suspend fun saveIotIdentity(identity: IotIdentityDto, privateKeyPem: String) {
        val (iv, encryptedKey) = CryptoUtils.encrypt(privateKeyPem)

        dataStore.edit { prefs ->
            prefs[THING_NAME] = identity.thingName!!
            prefs[CERT_PEM] = identity.certPem!!
            prefs[ENCRYPTED_KEY] = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
            prefs[KEY_IV] = Base64.encodeToString(iv, Base64.DEFAULT)
        }
    }

    suspend fun fetchDecryptKey(): String? {
        val prefs = dataStore.data.first()
        val encryptedKey = prefs[ENCRYPTED_KEY] ?: return null
        val iv = prefs[KEY_IV] ?: return null

        return CryptoUtils.decrypt(
            Base64.decode(iv, Base64.DEFAULT),
            Base64.decode(encryptedKey, Base64.DEFAULT)
        )
    }

    suspend fun fetchIotIdentity(): IotIdentityDto? {
        return dataStore.data
            .map { prefs ->
                val thingName = prefs[THING_NAME]
                val certPem = prefs[CERT_PEM]
                val iv = prefs[KEY_IV]
                val encryptedKey = prefs[ENCRYPTED_KEY]

                if (thingName != null && certPem != null && iv != null && encryptedKey != null) {
                    IotIdentityDto(
                        thingName = thingName, 
                        certPem = certPem,
                        iv = iv,
                        encryptedKey = encryptedKey
                    )
                } else {
                    null
                }
            }
            .firstOrNull()
    }

    suspend fun clearIotIdentity() {
        dataStore.edit { it.clear() }
        CryptoUtils.clearMasterKey()
    }
}

data class IotIdentityDto(
    val thingName: String?,
    val certPem: String?,
    val iv: String?,
    val encryptedKey: String?
)
