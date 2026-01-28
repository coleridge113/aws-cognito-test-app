package com.example.aws_cognito_test.data.utils

import com.auth0.android.jwt.JWT
import com.auth0.android.jwt.DecodeException

object JwtUtils {
    fun isJwtTokenExpired(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            jwt.isExpired(0) // Check if the token is expired with 0 seconds of grace period
        } catch (e: DecodeException) {
            // Log the exception if needed, for example, if the token is malformed
            false // Treat malformed token as not expired to avoid constant refreshing if it's invalid for other reasons
        } catch (e: IllegalArgumentException) {
            // Handle cases where the token string is null or empty
            true // Treat null/empty as expired
        }
    }
}
