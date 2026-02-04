package com.example.aws_cognito_test.domain.usecase

import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.domain.model.Certificates

class FetchCertificatesUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String): Certificates {
        return repository.fetchCertificates(token)
    }
}
