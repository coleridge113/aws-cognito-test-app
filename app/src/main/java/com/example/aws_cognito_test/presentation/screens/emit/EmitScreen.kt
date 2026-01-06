package com.example.aws_cognito_test.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aws_cognito_test.presentation.screens.emit.EmitStateEvents
import com.example.aws_cognito_test.presentation.screens.emit.EmitViewModel

const val TAG = "EmitScreen"

@Composable
fun EmitScreen(
    viewModel: EmitViewModel,
    navController: NavController
) {
    
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val onEvent = remember(viewModel) { viewModel::onEvent }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        MainContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onEvent = onEvent
        )      
    }
}

@Composable
fun MainContent(
    modifier: Modifier,
    uiState: EmitStateEvents.UiState,
    onEvent: (EmitStateEvents.Event) -> Unit
) {
    val lat = rememberTextFieldState()
    val lng = rememberTextFieldState()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                Log.d(TAG, "Success state: ${uiState.success}")
                if (!uiState.success) {
                    onEvent(EmitStateEvents.Event.StartEmit)
                } else {
                    onEvent(EmitStateEvents.Event.StopEmit)
                }
            }
        ) {
            val s = if (!uiState.success) "Start" else "Stop"
            Text(text = "$s Emitting")
        }
    }
}
