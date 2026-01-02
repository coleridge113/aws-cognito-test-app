package com.example.aws_cognito_test.presentation.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EmitViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmitStateEvents.UiState())
    val state: StateFlow<EmitStateEvents.UiState> = _state.asStateFlow()
    
    fun onEvent(event: EmitStateEvents.Event) {
        when (event) {
            EmitStateEvents.Event.StartEmit -> { startEmitting() }
            EmitStateEvents.Event.StopEmit -> { stopEmitting() }
        }
    }

    private fun startEmitting() {

        _state.update { curr ->
            curr.copy(
                success = true
            )
        }
        Log.d("EmitState", "Emitting: ${_state.value.success}")
    }

    private fun stopEmitting() {
        _state.update { curr ->
            curr.copy(
                success = false
            )
        }
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
