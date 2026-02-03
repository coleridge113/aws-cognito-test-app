package com.example.aws_cognito_test.data.remote.dto

data class CertResponse(
    val certificatePem: String,
    val privateKey: String,
    val expiration: String,
    val riderId: String
)

data class TokenRequest(val token: String)
