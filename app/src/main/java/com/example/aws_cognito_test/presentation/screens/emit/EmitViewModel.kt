package com.example.aws_cognito_test.presentation.screens.emit

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aws_cognito_test.data.database.entity.LocationEntity
import com.example.aws_cognito_test.domain.model.Location
import com.example.aws_cognito_test.data.mapper.toModel
import com.example.aws_cognito_test.data.utils.LocalFileLoader
import com.example.aws_cognito_test.data.utils.OSLocationManager
import com.example.aws_cognito_test.domain.repository.LocationRepository
import com.example.aws_cognito_test.domain.utils.TrackingManager
import com.example.aws_cognito_test.domain.utils.IotManager
import com.example.aws_cognito_test.domain.usecase.GetTokenUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import org.koin.androidx.compose.viewModel

class EmitViewModel(
    private val fileLoader: LocalFileLoader,
    private val locationRepository: LocationRepository,
    private val trackingManager: TrackingManager,
    private val locationManager: OSLocationManager,
    private val iotManager: IotManager,
    private val getTokenUseCase: GetTokenUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            getTokenUseCase()?.let { token ->
                // iotManager.initMqttClientWithCustom(token)
                iotManager.initMqttClientWithX()
            }
        }
    }

    private val _state = MutableStateFlow(EmitStateEvents.UiState())
    val state: StateFlow<EmitStateEvents.UiState> = _state.asStateFlow()

    private val _currentLocationState = MutableStateFlow<Location?>(null)
    val currentLocationState = _currentLocationState.asStateFlow()

    private var emitJob: Job? = null

    fun onEvent(event: EmitStateEvents.Event) {
        when (event) {
            is EmitStateEvents.Event.StartEmit -> { 
                startEmitting(event.deviceId, event.jobOrderId)
            }
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
    private fun startEmitting(
        deviceId: String = "",
        jobOrderId: String = ""
    ) {
        emitJob = viewModelScope.launch {
            _state.update { curr ->
                curr.copy(
                    isEmitting = true
                )
            }

            locationManager.requestPriorityGPS().collect { location ->
                Log.d("EmitViewModel", "Received: $location")
                val preferLiveUpdate = _state.value.isChecked
                val entity = LocationEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = System.currentTimeMillis()
                )

                _currentLocationState.emit(entity.toModel())

                try {
                    if (preferLiveUpdate) {
                        // trackingManager.updateLocationLive(deviceId, jobOrderId, entity.toModel())
                        iotManager.publishMessage(deviceId, jobOrderId, entity.toModel())
                    } else {
                        locationRepository.saveLocation(entity)
                    }
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
            val locations = locationRepository.getLocations().map {
                it.toModel()
            }
            try {
                trackingManager.batchUpdateLocation(
                    deviceId, 
                    jobOrderId,
                    locations
                )
                locationRepository.deleteLocations()
            } catch (e: HttpException) {
                Log.e("EmitViewModel", "Error uploading: ${e.message}")
            } catch (e: IOException) {
                Log.e("EmitViewModel", "Error repo: ${e.message}")
            }
        }
    }

    private fun evaluateGeo(deviceId: String, jobOrderId: String) {
        viewModelScope.launch {
            currentLocationState.value?.let {
                trackingManager.evaluateGeofence(deviceId, jobOrderId, it) 
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
        val isChecked: Boolean = true
    )

    sealed interface Event {
        data object StopEmit : Event
        data object ToggleCheckbox : Event
        data class StartEmit(val deviceId: String = "", val jobOrderId: String = "") : Event
        data class SendUpdates(val deviceId: String, val jobOrderId: String) : Event
        data class EvaluateGeo(val deviceId: String, val jobOrderId: String) : Event
    }

}
