package com.example.aws_cognito_test.presentation.screens.emit

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aws_cognito_test.data.database.entity.LocationEntity
import com.example.aws_cognito_test.data.mapper.toModel
import com.example.aws_cognito_test.data.utils.LocalFileLoader
import com.example.aws_cognito_test.data.utils.OSLocationManager
import com.example.aws_cognito_test.domain.repository.LocationRepository
import com.example.aws_cognito_test.domain.utils.TrackingManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class EmitViewModel(
    private val fileLoader: LocalFileLoader,
    private val repository: LocationRepository,
    private val trackingManager: TrackingManager,
    private val locationManager: OSLocationManager
) : ViewModel() {

    private val _state = MutableStateFlow(EmitStateEvents.UiState())
    val state: StateFlow<EmitStateEvents.UiState> = _state.asStateFlow()

    private var emitJob: Job? = null

    fun onEvent(event: EmitStateEvents.Event) {
        when (event) {
            EmitStateEvents.Event.StartEmit -> { startEmitting() }
            EmitStateEvents.Event.StopEmit -> { stopEmitting() }
            EmitStateEvents.Event.ToggleCheckbox -> { toggleCheckbox() }
            is EmitStateEvents.Event.SendUpdates -> { sendUpdates(
                event.deviceId,
                event.jobOrderId
            ) }
            is EmitStateEvents.Event.EvaluateGeo -> {
                evaluateGeo(
                    event.deviceId,
                    event.jobOrderId
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startEmitting() {
        emitJob = viewModelScope.launch {
            _state.update { curr ->
                curr.copy(
                    isEmitting = true
                )
            }
            // fileLoader.loadRoutePoints().collect { location ->
            //     trackingManager.updateLocation(location)
            // }

            locationManager.requestPriorityGPS().collect { location ->
                Log.d("EmitViewModel", "Received: $location")
                val entity = LocationEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )
                try {
                    repository.saveLocation(entity)
                } catch (e: IOException) {
                    Log.e("EmitViewModel", "Failed to save: ${e.message}")
                }
            }
        }

        Log.d("EmitState", "Emitting: ${_state.value.isEmitting}")
    }

    private fun stopEmitting() {
        _state.update { curr ->
            curr.copy(
                isEmitting = false
            )
        }
        emitJob?.cancel()
        Log.d("EmitState", "Emitting: ${_state.value.isEmitting}")
    }

    private fun sendUpdates(deviceId: String, jobOrderId: String) {
        viewModelScope.launch {
            val locations = repository.getLocations().map {
                it.toModel()
            }
            try {
                trackingManager.batchUpdateLocation(
                    deviceId, 
                    jobOrderId,
                    locations
                )
                repository.deleteLocations()
            } catch (e: HttpException) {
                Log.e("EmitViewModel", "Error uploading: ${e.message}")
            } catch (e: IOException) {
                Log.e("EmitViewModel", "Error repo: ${e.message}")
            }
        }
    }

    private fun evaluateGeo(deviceId: String, jobOrderId: String) {
        viewModelScope.launch {
            val lastLocation = repository.getLastLocation()?.toModel()
            
            lastLocation?.let { location ->
                trackingManager.evaluateGeofence(deviceId, jobOrderId, location)
            }
        }
    }

    private fun toggleCheckbox() {
        viewModelScope.launch {
            _state.update { curr ->
                curr.copy(
                    isChecked = !curr.isChecked
                )   
            }
        }
    }
}

object EmitStateEvents {

    data class UiState(
        val isLoading: Boolean = false,
        val error: String = "",
        val isEmitting: Boolean = false,
        val isChecked: Boolean = false
    )

    sealed interface Event {
        data object StartEmit : Event
        data object StopEmit : Event
        data object ToggleCheckbox : Event
        data class SendUpdates(val deviceId: String, val jobOrderId: String) : Event
        data class EvaluateGeo(val deviceId: String, val jobOrderId: String) : Event
    }

}
