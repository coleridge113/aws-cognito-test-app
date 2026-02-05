package com.example.aws_cognito_test.domain.usecase

import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.domain.model.IotIdentity

class SetCertificateKeysUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(identity: IotIdentity, privateKeyPem: String) {
        repository.saveIotIdentity(identity, privateKeyPem)
    }
}
