package com.example.aws_cognito_test.data.database.dao

import com.example.aws_cognito_test.data.database.entity.Location
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Dao

@Dao
interface LocationDao {
    
    @Insert
    suspend fun saveLocation(location: Location)

    @Query("SELECT * FROM location ORDER BY timestamp")
    suspend fun getLocations(): List<Location>

    @Query("DELETE FROM location")
    suspend fun deleteLocations()

}
