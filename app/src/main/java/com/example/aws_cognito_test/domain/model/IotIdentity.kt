package com.example.aws_cognito_test.domain.model

data class IotIdentity(
    val thingName: String?,
    val certPem: String?,
    val iv: String?,
    val encryptedKey: String?
)
