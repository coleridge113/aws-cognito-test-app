package com.example.aws_cognito_test

import android.app.Application
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.configuration.AmplifyOutputs
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import android.util.Log

class AwsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
           Amplify.addPlugin(AWSCognitoAuthPlugin())
           Amplify.configure(AmplifyOutputs(R.raw.amplify_outputs), applicationContext)
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (e: Exception) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", e)
        }
       
    }
}
