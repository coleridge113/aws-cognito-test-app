package com.example.aws_cognito_test.domain.repository

import com.example.aws_cognito_test.data.database.entity.Location

interface LocationRepository {

    suspend fun saveLocation(location: Location)

    suspend fun getLocations(): List<Location>

    suspend fun deleteLocations()

}
