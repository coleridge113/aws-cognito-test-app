package com.example.aws_cognito_test.domain.repository

interface AuthRepository {

    suspend fun refreshJwtToken(): String 

    suspend fun fetchJwtToken(): String?
}
