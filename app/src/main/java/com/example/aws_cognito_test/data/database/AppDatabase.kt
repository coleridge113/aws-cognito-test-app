package com.example.aws_cognito_test.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.aws_cognito_test.data.database.dao.LocationDao
import com.example.aws_cognito_test.data.database.entity.Location

@Database(
    entities = [Location::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao

}
