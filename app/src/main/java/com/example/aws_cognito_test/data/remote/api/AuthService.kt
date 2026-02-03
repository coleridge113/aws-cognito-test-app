package com.example.aws_cognito_test.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import com.example.aws_cognito_test.data.remote.dto.AuthResponse
import com.example.aws_cognito_test.data.remote.dto.CertResponse
import com.example.aws_cognito_test.data.remote.dto.TokenRequest

interface AuthService {

    @POST("/login")
    suspend fun fetchToken(): AuthResponse

    @POST("/get-iot-claim")
    suspend fun fetchCertificates(@Body token: TokenRequest): CertResponse
}
