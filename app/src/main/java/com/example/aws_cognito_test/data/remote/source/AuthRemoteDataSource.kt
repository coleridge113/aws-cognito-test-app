package com.example.aws_cognito_test.data.remote.source

import com.example.aws_cognito_test.data.remote.api.AuthService
import com.example.aws_cognito_test.data.remote.dto.AuthResponse
import com.example.aws_cognito_test.data.remote.dto.CertResponse
import com.example.aws_cognito_test.data.remote.dto.TokenRequest

class AuthRemoteDataSource(
    private val api: AuthService
) {

    suspend fun fetchJwtToken(): AuthResponse {
        return api.fetchToken()
    }

    suspend fun fetchCertificates(riderId: String, token: String): CertResponse {
        return api.fetchCertificates(TokenRequest(riderId, token))
    }
}
