package com.example.aws_cognito_test.domain.repository

import com.example.aws_cognito_test.domain.model.Certificates
import com.example.aws_cognito_test.domain.model.IotIdentity
import java.security.PrivateKey

interface AuthRepository {

    suspend fun refreshJwtToken(): String 

    suspend fun fetchJwtToken(): String?
    
    suspend fun fetchCertificates(token: String): Certificates

    suspend fun saveIotIdentity(identity: IotIdentity, privateKeyPem: String)

    suspend fun fetchIotIdentity(): IotIdentity?

    suspend fun savePrivateKeyToKeystore(alias: String, privateKeyPem: String, certPem: String)

    suspend fun getPrivateKeyFromKeystore(alias: String): PrivateKey?

    suspend fun clearIdentityAndKeys()
}
