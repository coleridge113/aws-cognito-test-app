package com.example.aws_cognito_test.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sequence: Int = 0,
    val longitude: Double,
    val latitude: Double,
    val timestamp: Long
)
