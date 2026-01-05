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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmitViewModel(
    private val application: Application,
    private val trackingManager: TrackingManager
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext
    private val _state = MutableStateFlow(EmitStateEvents.UiState())
    val state: StateFlow<EmitStateEvents.UiState> = _state.asStateFlow()
    
    fun onEvent(event: EmitStateEvents.Event) {
        when (event) {
            is EmitStateEvents.Event.StartEmit -> { 
                startEmitting(event.latitude, event.longitude)
            }
            EmitStateEvents.Event.StopEmit -> { stopEmitting() }
        }
    }

    private fun startEmitting(lat: String, lng: String) {
        viewModelScope.launch {
            _state.update { curr ->
                curr.copy(
                    success = true
                )
            }
            trackingManager.updateLocation(lat, lng)
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

    init {
        viewModelScope.launch {
            trackingManager.updateLocation()
        }
    }
}

object EmitStateEvents {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val success: Boolean = false
    )

    sealed interface Event {
        data class StartEmit(val latitude: String, val longitude: String) : Event
        data object StopEmit : Event
    }
}
