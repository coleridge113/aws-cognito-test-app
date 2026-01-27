package com.example.aws_cognito_test.data.remote.api

import retrofit2.http.Body
import retrofit2.http.POST
import com.example.aws_cognito_test.data.remote.dto.AuthResponse

interface AuthService {

    @POST("/login")
    fun fetchToken(): AuthResponse

}
