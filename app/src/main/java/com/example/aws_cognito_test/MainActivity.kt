package com.example.aws_cognito_test

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.amplifyframework.core.Amplify
import com.example.aws_cognito_test.ui.theme.AwscognitotestTheme
import com.amplifyframework.ui.authenticator.ui.Authenticator

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AwscognitotestTheme {
                var user by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    Amplify.Auth.fetchUserAttributes(
                        { attributes ->
                            user = attributes.firstOrNull() { it.key.keyString == "name" }?.value.toString()
                        },
                        { error ->
                            Log.i("AuthDemo", "Failed to fetch user attributes: $error")
                        }
                    )
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Authenticator { state ->
                            Column {
                                Text(
                                    text = "Hello $user"
                                )
                                Button(
                                    onClick = {
                                        Amplify.Auth.signOut {}
                                    }
                                ) {
                                    Text(text = "Sign Out")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
