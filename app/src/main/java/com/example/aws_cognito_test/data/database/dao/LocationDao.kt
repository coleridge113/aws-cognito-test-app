package com.example.aws_cognito_test.data.database.dao

import com.example.aws_cognito_test.data.database.entity.LocationEntity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao

@Dao
interface LocationDao {
    
    @Insert
    suspend fun saveLocation(location: LocationEntity)

    @Insert
    suspend fun saveLocations(locations: List<LocationEntity>)

    @Query("SELECT * FROM location ORDER BY timestamp")
    suspend fun getLocations(): List<LocationEntity>

    @Query("SELECT * FROM location ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(): LocationEntity?

    @Query("DELETE FROM location")
    suspend fun deleteLocations()

}
