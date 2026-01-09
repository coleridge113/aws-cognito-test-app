package com.example.aws_cognito_test.presentation.screens.emit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
    ) {
        val focusManager = LocalFocusManager.current
        val deviceIdState = rememberTextFieldState("Device-")
        val jobOrderState = rememberTextFieldState("JobOrder-")

        OutlinedTextField(
            state = deviceIdState,
            label = { Text(text = "Device ID") },
        )

        Spacer(modifier = Modifier.padding(top = 10.dp))

        OutlinedTextField(
            state = jobOrderState,
            label = { Text(text = "Job Order #") },
        )

        Spacer(modifier = Modifier.padding(top = 20.dp))

        Box {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (!uiState.isEmitting) {
                        onEvent(EmitStateEvents.Event.StartEmit)
                    } else {
                        onEvent(EmitStateEvents.Event.StopEmit)
                    }
                }
            ) {
                val action = if (!uiState.isEmitting) "Start" else "Stop"
                val preference = if (uiState.isChecked) "Emitting" else "Storing"
                Text(text = "$action $preference")
            }

            Switch(
                checked = uiState.isChecked,
                onCheckedChange = {
                    onEvent(EmitStateEvents.Event.ToggleCheckbox)
                },
                modifier = Modifier.offset(x = (-75).dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(!uiState.isChecked) {
            Column {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val deviceId = deviceIdState.text.toString()
                        val jobOrderId = jobOrderState.text.toString()
                        onEvent(EmitStateEvents.Event.SendUpdates(deviceId, jobOrderId))
                    }
                ) {
                    Text(text = "Send Updates")
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                val deviceId = deviceIdState.text.toString()
                val jobOrderId = jobOrderState.text.toString()
                onEvent(EmitStateEvents.Event.EvaluateGeo(deviceId, jobOrderId))
            } 
        ) {
            Text(text = "Evaluate Geo")
        }
    }
}
