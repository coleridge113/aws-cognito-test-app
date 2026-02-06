package com.example.aws_cognito_test.data.local

import com.example.aws_cognito_test.data.remote.source.AuthRemoteDataSource
import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.domain.model.Certificates
import com.example.aws_cognito_test.data.datastore.AuthLocalDataSource
import com.example.aws_cognito_test.data.datastore.IotLocalDataSource
import com.example.aws_cognito_test.data.utils.JwtUtils
import com.example.aws_cognito_test.data.utils.KeyStoreUtils
import com.example.aws_cognito_test.data.mapper.toModel
import com.example.aws_cognito_test.data.mapper.toDto
import com.example.aws_cognito_test.domain.model.IotIdentity
import android.util.Log
import java.security.PrivateKey

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
    private val iotLocal: IotLocalDataSource
): AuthRepository {
    
    override suspend fun refreshJwtToken(): String {
        val token = remote.fetchJwtToken().token
        local.saveJwtToken(token)
        Log.d("AuthToken", "Successfully fetched token: $token")
        return token
    }

    override suspend fun fetchJwtToken(): String? {
        val stored = local.getJwtToken()
        return if (stored.isNullOrEmpty() || JwtUtils.isJwtTokenExpired(stored)) {
            refreshJwtToken()
        } else {
            stored
        }
    }

    override suspend fun fetchCertificates(riderId: String, token: String): Certificates {
        return remote.fetchCertificates(riderId, token).toModel()
    }

    override suspend fun saveIotIdentity(identity: IotIdentity, privateKeyPem: String) {
        iotLocal.saveIotIdentity(identity.toDto(), privateKeyPem)
    }

    override suspend fun fetchIotIdentity(): IotIdentity? {
        return iotLocal.fetchIotIdentity()?.toModel()
    }

    override suspend fun savePrivateKeyToKeystore(alias: String, privateKeyPem: String, certPem: String) {
        KeyStoreUtils.savePrivateKeyToKeystore(alias, privateKeyPem, certPem)
    }

    override suspend fun getPrivateKeyFromKeystore(alias: String): PrivateKey? {
        return KeyStoreUtils.getPrivateKeyFromKeystore(alias)
    }

    override suspend fun clearIdentityAndKeys() {
        iotLocal.clearIotIdentity()
    }
}
