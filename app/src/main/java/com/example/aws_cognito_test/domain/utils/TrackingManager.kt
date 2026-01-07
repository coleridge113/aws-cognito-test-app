package com.example.aws_cognito_test.domain.utils

import android.util.Log
import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.batchUpdateDevicePosition
import aws.sdk.kotlin.services.location.model.DevicePositionUpdate
import aws.smithy.kotlin.runtime.time.Clock
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.location.AWSLocationGeoPlugin
import com.example.aws_cognito_test.domain.model.Location

const val TAG = "TrackingManager"

class TrackingManager {
    private val trackerName = "MetromartDemoTracker"

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
            trackerName = "MetromartDemoTracker"
            updates = listOf(
                positionUpdate
            )
        }
        Log.d(TAG, "Successfully sent location: $location")
    }

}
