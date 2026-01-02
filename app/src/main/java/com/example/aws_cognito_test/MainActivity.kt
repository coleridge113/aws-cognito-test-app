package com.example.aws_cognito_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.compose.rememberNavController
import com.example.aws_cognito_test.presentation.NavGraphSetup
import com.example.aws_cognito_test.ui.theme.AwscognitotestTheme

@ExperimentalMaterial3Api
@ExperimentalSharedTransitionApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AwscognitotestTheme {
                val navController = rememberNavController()
                NavGraphSetup(navController = navController)
            }
        }
    }
}
