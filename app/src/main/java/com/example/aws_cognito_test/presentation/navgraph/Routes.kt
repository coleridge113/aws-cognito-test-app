package com.example.aws_cognito_test.presentation

import kotlinx.serialization.Serializable

@Serializable
sealed class Routes {

    @Serializable
    data object LoginRoute : Routes()

    @Serializable
    data object EmitRoute : Routes()

}
