package com.example.aws_cognito_test.domain.repository

import com.example.aws_cognito_test.domain.model.Certificates

interface AuthRepository {

    suspend fun refreshJwtToken(): String 

    suspend fun fetchJwtToken(): String?
    
    suspend fun fetchCertificates(token: String): Certificates
}
