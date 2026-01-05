package com.example.aws_cognito_test.domain.utils

import android.util.Log
import aws.sdk.kotlin.services.location.LocationClient
import aws.sdk.kotlin.services.location.batchUpdateDevicePosition
import aws.sdk.kotlin.services.location.model.DevicePositionUpdate
import aws.smithy.kotlin.runtime.time.Clock
import com.amplifyframework.core.Amplify
import com.amplifyframework.geo.location.AWSLocationGeoPlugin

const val TAG = "TrackingManager"

class TrackingManager {
     private val trackerName = "MetromartDemoTracker"

     private val geoPlugin: AWSLocationGeoPlugin by lazy { 
         Amplify.Geo.getPlugin("awsLocationGeoPlugin") as AWSLocationGeoPlugin
     }
     private val client: LocationClient by lazy { geoPlugin.escapeHatch }

     suspend fun updateLocation(
         latitude: String = "13.9736382",
         longitude: String = "121.6761132"
     ) {
         val lat = latitude.toDouble()
         val lng = longitude.toDouble()

         val positionUpdate = DevicePositionUpdate {
             deviceId = "Device-1"
             position = listOf(lng, lat)
             sampleTime = Clock.System.now()
         }

         client.batchUpdateDevicePosition {
             trackerName = "MetromartDemoTracker"
             updates = listOf(
                positionUpdate
             )
         }
         Log.d(TAG, "Successfully sent location: $positionUpdate")
     }

//    fun initTracker(context: Context) {
//        val credentialsProvider = LocationCredentialsProvider(context)
//        credentialsProvider.initializeLocationClient(provider)
//
//        tracker = LocationTracker(
//            context,
//            credentialsProvider,
//            trackerName
//        )
//    }
}
