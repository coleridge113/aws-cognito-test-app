package com.example.aws_cognito_test.data.mapper

import com.example.aws_cognito_test.data.remote.dto.CertResponse
import com.example.aws_cognito_test.domain.model.Certificates

fun CertResponse.toModel(): Certificates {
    return Certificates(
        certificatePem = this.certificatePem,
        privateKey = this.privateKey,
        expiration = this.expiration,
        riderId = this.riderId
    )
}
