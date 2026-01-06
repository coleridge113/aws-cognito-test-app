package com.example.aws_cognito_test.data.utils

import android.util.Log
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.aws_cognito_test.domain.model.Location
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val TAG = "LocalFileReader" 

class LocalFileLoader(
    private val context: Context
) {
    suspend fun loadRoutePoints(): Flow<Location> {
        return flow {
            try {
                val inputStream = context.assets.open("location_data.txt")
                val reader = BufferedReader(InputStreamReader(inputStream))
                reader.useLines { lines ->
                    lines.forEachIndexed { idx, line ->
                        val trimmed = line.trim()
                        if (trimmed.isEmpty()) return@forEachIndexed
                        val parts = trimmed.split(',')
                        if (parts.size != 2) return@forEachIndexed

                        val lat = parts[0].toDoubleOrNull()
                        val lng = parts[1].toDoubleOrNull()
                        if (lat != null && lng != null) {
                            val location = Location(
                                sequence = idx,
                                latitude = lat,
                                longitude = lng,
                                timestamp = System.currentTimeMillis()
                            )                       
                            emit(location)
                        }
                        delay(2000)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read location_data.txt: ${e.message}", e)
            }
        }
    }
}
