package com.example.aws_cognito_test.data.utils

import android.util.Log
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

const val TAG = "LocalFileReader" 

class LocalFileLoader(
    private val context: Context
) {
    suspend fun loadRoutePoints(): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        return try {
            val inputStream = context.assets.open("location_data.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEach
                    val parts = trimmed.split(',')
                    if (parts.size != 2) return@forEach

                    val lat = parts[0].toDoubleOrNull()
                    val lng = parts[1].toDoubleOrNull()
                    if (lat != null && lng != null) {
                        result.add(lng to lat)
                    }
                }
            }

            Log.d(TAG, "Loaded ${result.size} points from assets/location_data.txt")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read location_data.txt: ${e.message}", e)
            emptyList()
        }
    }
}
