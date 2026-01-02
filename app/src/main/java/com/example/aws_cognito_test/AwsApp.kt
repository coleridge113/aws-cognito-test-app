package com.example.aws_cognito_test

import android.app.Application
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.example.aws_cognito_test.di.appModule
import android.util.Log
import com.amplifyframework.geo.location.AWSLocationGeoPlugin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AwsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AwsApp)
            modules(appModule)
        }
        try {
           Amplify.addPlugin(AWSCognitoAuthPlugin())
           Amplify.addPlugin(AWSLocationGeoPlugin())
           Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (e: Exception) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", e)
        }
       
    }
}
