package com.example.aws_cognito_test.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aws_cognito_test.data.database.dao.LocationDao
import com.example.aws_cognito_test.data.database.entity.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

}
