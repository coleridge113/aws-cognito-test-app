package com.example.aws_cognito_test.data.local

import com.example.aws_cognito_test.data.database.dao.LocationDao
import com.example.aws_cognito_test.data.database.entity.LocationEntity
import com.example.aws_cognito_test.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val dao: LocationDao
) : LocationRepository {

    override suspend fun saveLocation(location: LocationEntity) {
        dao.saveLocation(location)
    }

    override suspend fun saveLocations(locations: List<LocationEntity>) {
        dao.saveLocations(locations)
   }

    override suspend fun getLocations(): List<LocationEntity> {
        return dao.getLocations()
    }

    override suspend fun deleteLocations() {
        dao.deleteLocations()
    }

}
