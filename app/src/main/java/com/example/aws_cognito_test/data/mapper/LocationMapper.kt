package com.example.aws_cognito_test.data.mapper

import com.example.aws_cognito_test.data.database.entity.LocationEntity
import com.example.aws_cognito_test.domain.model.Location

fun LocationEntity.toModel(): Location {
    return Location(
        sequence = this.sequence,
        longitude = this.longitude,
        latitude = this.latitude,
        timestamp = this.timestamp
    )
}

fun Location.toEntity(): LocationEntity {
    return LocationEntity(
        sequence = this.sequence,
        longitude = this.longitude,
        latitude = this.latitude,
        timestamp = this.timestamp
    )
}
