package com.example.aws_cognito_test.domain.model

import java.time.Instant

data class Certificates(
    val certificatePem: String,
    val privateKey: String,
    val expiration: String,
    val riderId: String
)


