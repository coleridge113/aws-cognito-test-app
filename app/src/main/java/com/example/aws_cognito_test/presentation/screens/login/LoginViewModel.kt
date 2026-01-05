package com.example.aws_cognito_test.presentation.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class LoginViewModel : ViewModel() {
    private val _navigation = Channel<LoginViewModelStateEvents.Navigation>()
    val navigation: Flow<LoginViewModelStateEvents.Navigation> = _navigation.receiveAsFlow()

    private val _state = MutableStateFlow(LoginViewModelStateEvents.UiState())
    val state: StateFlow<LoginViewModelStateEvents.UiState> = _state.asStateFlow()

    fun onEvent(event: LoginViewModelStateEvents.Event) {
        when (event) {
            LoginViewModelStateEvents.Event.FetchAttributes -> { fetchUserAttributes() }
            LoginViewModelStateEvents.Event.SignOut -> { signOut() }
            LoginViewModelStateEvents.Event.GoToEmitScreen -> { goToEmitScreen() }
        }
    }

    private fun fetchUserAttributes() {
        viewModelScope.launch {
            Amplify.Auth.fetchUserAttributes(
                { attributes ->
                    _state.update { currentState ->
                        currentState.copy(
                            success = attributes.firstOrNull {
                                it.key.keyString == "name"
                            }?.value.toString()
                        )
                    }
                    goToEmitScreen()
                    Log.d("Authentication", "Successfully logged in: ${_state.value}")
                },
                { error ->
                    _state.update { currentState ->
                        currentState.copy(
                            error = error.message
                        )
                    }
                    Log.w("Authentication", "Failed to login: ${error.message}")
                }
            )
        }
    }

    private fun goToEmitScreen() {
        viewModelScope.launch {
            delay(2000)
            _navigation.send(LoginViewModelStateEvents.Navigation.GoToEmitScreen)
        }
    }

    private fun signOut() {
        Amplify.Auth.signOut {}
    }
}

object LoginViewModelStateEvents {
    data class UiState(
        val isLoading: Boolean = false,
        val error: String? = "",
        val success: String? = ""
    )

    sealed interface Event {
        data object FetchAttributes : Event
        data object SignOut : Event
        data object GoToEmitScreen : Event
    }

    sealed class Navigation {
        data object GoToEmitScreen : Navigation()
    }
}

