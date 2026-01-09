package com.example.aws_cognito_test.domain.utils

import android.util.Log
import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.batchEvaluateGeofences
import aws.sdk.kotlin.services.location.batchUpdateDevicePosition
import aws.sdk.kotlin.services.location.model.DevicePositionUpdate
import aws.smithy.kotlin.runtime.time.Clock
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import aws.smithy.kotlin.runtime.http.HttpException
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.location.AWSLocationGeoPlugin
import com.example.aws_cognito_test.domain.model.Location

const val TAG = "TrackingManager"

class TrackingManager {
    private val trackerResource = "MetromartDemoTracker"
    private val geofenceCollection = "MetromartDemoGeofenceCollection"

    private val geoPlugin: AWSLocationGeoPlugin by lazy { 
        Amplify.Geo.getPlugin("awsLocationGeoPlugin") as AWSLocationGeoPlugin
    }
    private val client: LocationClient by lazy { geoPlugin.escapeHatch }

    suspend fun updateLocation(location: Location) {
        val lat = location.latitude
        val lng = location.longitude

        val positionUpdate = DevicePositionUpdate {
            deviceId = "Device-2"
            position = listOf(lng, lat)
            sampleTime = Clock.System.now()
        }

        client.batchUpdateDevicePosition {
            trackerName = trackerResource
            updates = listOf(
                positionUpdate
            )
        }
        Log.d(TAG, "Successfully sent location: $location")
    }

    suspend fun batchUpdateLocation(
        id: String, 
        jobOrderId: String,
        locations: List<Location>
    ) {
        val chunks = locations.chunked(10)
        val properties = mapOf(Pair("jobOrderId", jobOrderId))

        for(chunk in chunks) {
            val updates = chunk.map { loc ->
                DevicePositionUpdate {
                    deviceId = id
                    position = listOf(loc.longitude, loc.latitude)
                    sampleTime = convertTimestampToInstant(loc.timestamp)
                    positionProperties = properties
                }
            }
            try {
                client.batchUpdateDevicePosition {
                    trackerName = trackerResource
                    this.updates = updates
                }
            } catch (e: HttpException) {
                Log.e(TAG, "No internet: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.message}")
            }
            Log.d(TAG, "Uploaded: $updates")
        }

    }

    suspend fun evaluateGeofence(id: String, jobOrderId: String, location: Location) {
        val properties = mapOf(Pair("jobOrderId", jobOrderId))
        val update = DevicePositionUpdate {
            deviceId = id
            position = listOf(location.latitude, location.longitude)
            sampleTime = convertTimestampToInstant(location.timestamp)
            positionProperties = properties

        }
        try {
            client.batchEvaluateGeofences {
                collectionName = geofenceCollection
                devicePositionUpdates = listOf(update)
            }
            Log.d(TAG, "Sent location for geo evaluation")
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating location: ${e.message}")
        }
    }

    private fun convertTimestampToInstant(timestamp: Long): Instant {
        return Instant.fromEpochMilliseconds(timestamp)
    }
}
