package com.example.aws_cognito_test.presentation.screens.emit

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aws_cognito_test.presentation.screens.TAG
import com.example.aws_cognito_test.domain.utils.TrackingManager
import com.example.aws_cognito_test.data.utils.LocalFileLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import aws.smithy.kotlin.runtime.time.Clock
import com.example.aws_cognito_test.domain.model.Location
import kotlinx.coroutines.Job

class EmitViewModel(
    private val trackingManager: TrackingManager,
    private val fileLoader: LocalFileLoader
) : ViewModel() {

    private val _state = MutableStateFlow(EmitStateEvents.UiState())
    val state: StateFlow<EmitStateEvents.UiState> = _state.asStateFlow()

    private var emitJob: Job? = null
    
    fun onEvent(event: EmitStateEvents.Event) {
        when (event) {
            EmitStateEvents.Event.StartEmit -> { startEmitting() }
            EmitStateEvents.Event.StopEmit -> { stopEmitting() }
        }
    }

    private fun startEmitting() {
        emitJob = viewModelScope.launch {
            _state.update { curr ->
                curr.copy(
                    success = true
                )
            }

            val dataPoints = fileLoader.loadRoutePoints()

            dataPoints.forEachIndexed { idx, point ->
                Location(
                    sequence = idx,
                    latitude = point.first,
                    longitude = point.second,
                    timestamp = System.currentTimeMillis()
                ).also {
                    trackingManager.updateLocation(it)
                    delay(3000)
                }
            }
        }
        Log.d("EmitState", "Emitting: ${_state.value.success}")
    }

    private fun stopEmitting() {
        _state.update { curr ->
            curr.copy(
                success = false
            )
        }
        emitJob?.cancel()
        Log.d("EmitState", "Emitting: ${_state.value.success}")
    }
}

object EmitStateEvents {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val success: Boolean = false
    )

    sealed interface Event {
        data object StartEmit : Event
        data object StopEmit : Event
    }

}
