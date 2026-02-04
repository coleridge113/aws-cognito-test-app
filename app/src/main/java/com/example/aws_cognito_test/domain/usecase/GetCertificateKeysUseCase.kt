package com.example.aws_cognito_test.domain.usecase

import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.domain.model.IotIdentity

class GetCertificateKeysUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): IotIdentity? {
        return repository.fetchIotIdentity()
    }
}
