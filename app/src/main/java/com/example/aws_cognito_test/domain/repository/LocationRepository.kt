package com.example.aws_cognito_test.domain.repository

import com.example.aws_cognito_test.data.database.entity.LocationEntity

interface LocationRepository {

    suspend fun saveLocation(location: LocationEntity)

    suspend fun getLocations(): List<LocationEntity>

    suspend fun deleteLocations()

}
