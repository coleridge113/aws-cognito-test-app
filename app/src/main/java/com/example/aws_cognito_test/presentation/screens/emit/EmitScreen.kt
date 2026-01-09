package com.example.aws_cognito_test.presentation.screens.emit

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.content.ContextCompat

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
    val context = LocalContext.current
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val isGranted = permissionsResult.values.all { it }
        if (isGranted) {
            onEvent(EmitStateEvents.Event.StartEmit)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        Button(
            onClick = {
                focusManager.clearFocus()
                if (!uiState.success) {
                    val arePermissionsGranted = permissions.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }

                    if (arePermissionsGranted) {
                        onEvent(EmitStateEvents.Event.StartEmit)
                    } else {
                        launcher.launch(permissions)
                    }
                } else {
                    onEvent(EmitStateEvents.Event.StopEmit)
                }
            }
        ) {
            val s = if (!uiState.success) "Start" else "Stop"
            Text(text = "$s Storing")
        }

        Spacer(modifier = Modifier.padding(top = 20.dp))
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

        Spacer(modifier = Modifier.padding(top = 20.dp))
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
