package com.example.aws_cognito_test.data.remote.source

import com.example.aws_cognito_test.data.remote.api.AuthService
import com.example.aws_cognito_test.data.remote.dto.AuthResponse

class AuthRemoteDataSource(
    private val api: AuthService
) {

    suspend fun fetchJwtToken(): AuthResponse {
        return api.fetchToken()
    }
}
