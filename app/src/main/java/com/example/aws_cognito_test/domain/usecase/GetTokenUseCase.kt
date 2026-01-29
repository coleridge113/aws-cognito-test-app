package com.example.aws_cognito_test.domain.usecase

import com.example.aws_cognito_test.domain.repository.AuthRepository

class GetTokenUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): String? {
        return repository.fetchJwtToken()
    }
}
