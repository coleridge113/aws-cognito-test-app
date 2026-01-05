package com.example.aws_cognito_test.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val sequence: Int,
    val longitude: Double,
    val latitude: Double,
    val timestamp: Long
)
