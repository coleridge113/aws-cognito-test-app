package com.example.aws_cognito_test.data.mapper

import com.example.aws_cognito_test.domain.model.IotIdentity
import com.example.aws_cognito_test.data.datastore.IotIdentityDto

fun IotIdentityDto.toModel(): IotIdentity {
    return IotIdentity(
        thingName = this.thingName,
        certPem = this.certPem,
        iv = this.iv,
        encryptedKey = this.encryptedKey
    )
}

fun IotIdentity.toDto(): IotIdentityDto {
    return IotIdentityDto(
        thingName = this.thingName,
        certPem = this.certPem,
        iv = this.iv,
        encryptedKey = this.encryptedKey
    )
}
