package com.example.aws_cognito_test.data.local

import com.example.aws_cognito_test.data.remote.source.AuthRemoteDataSource
import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.data.datastore.AuthLocalDataSource
import com.example.aws_cognito_test.data.utils.JwtUtils
import android.util.Log

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource
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
}
