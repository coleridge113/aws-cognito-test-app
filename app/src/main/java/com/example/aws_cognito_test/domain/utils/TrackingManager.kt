package com.example.aws_cognito_test.domain.utils

import android.util.Log
import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.batchUpdateDevicePosition
import aws.sdk.kotlin.services.location.model.DevicePositionUpdate
import aws.smithy.kotlin.runtime.time.Clock
import aws.smithy.kotlin.runtime.time.Instant
import aws.smithy.kotlin.runtime.time.fromEpochMilliseconds
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.location.AWSLocationGeoPlugin
import com.example.aws_cognito_test.domain.model.Location
import kotlin.time.ExperimentalTime

const val TAG = "TrackingManager"

class TrackingManager {
    private val trackerResource = "MetromartDemoTracker"

    private val geoPlugin: AWSLocationGeoPlugin by lazy { 
        Amplify.Geo.getPlugin("awsLocationGeoPlugin") as AWSLocationGeoPlugin
    }
    private val client: LocationClient by lazy { geoPlugin.escapeHatch }

    suspend fun updateLocation(location: Location) {
        val lat = location.latitude
        val lng = location.longitude

        val positionUpdate = DevicePositionUpdate {
            deviceId = "Device-3"
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

    suspend fun batchUpdateLocation(locations: List<Location>) {
        val chunks = locations.chunked(10)

        for(chunk in chunks) {
            val updates = chunk.map { loc ->
                DevicePositionUpdate {
                    deviceId = "Device-3"
                    position = listOf(loc.longitude, loc.latitude)
                    sampleTime = convertTimestampToInstant(loc.timestamp)
                }
            }
            client.batchUpdateDevicePosition {
                trackerName = trackerResource
                this.updates = updates
            }
            Log.d(TAG, "Uploaded: $updates")
        }

    }

    private fun convertTimestampToInstant(timestamp: Long): Instant {
        return Instant.fromEpochMilliseconds(timestamp)
    }
}
