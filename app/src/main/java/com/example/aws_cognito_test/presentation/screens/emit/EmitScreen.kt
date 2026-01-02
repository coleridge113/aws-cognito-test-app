package com.example.aws_cognito_test.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun EmitScreen(
    viewModel: EmitViewModel,
    navController: NavController
) {
    
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        MainContent(
            modifier = Modifier.padding(innerPadding)
        )      
    }
}

@Composable
fun MainContent(
    modifier: Modifier
) {}
