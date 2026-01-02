package com.example.aws_cognito_test.presentation.screens.login

import androidx.lifecycle.ViewModel
import com.amplifyframework.core.Amplify
import com.amplifyframework.ui.authenticator.AuthenticatorStepState
import com.amplifyframework.ui.authenticator.LoadingState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class LoginViewModel : ViewModel() {
    private val _navigation = Channel<LoginViewModelStateEvents.Navigation>()
    val navigation: Flow<LoginViewModelStateEvents.Navigation> = _navigation.receiveAsFlow()

    private val _state = MutableStateFlow(LoginViewModelStateEvents.UiState())
    val state: StateFlow<LoginViewModelStateEvents.UiState> = _state.asStateFlow()

    fun onEvent(event: LoginViewModelStateEvents.Event) {
        when (event) {
            LoginViewModelStateEvents.Event.SignIn -> { fetchUserAttributes() }
            LoginViewModelStateEvents.Event.SignOut -> { signOut() }
            LoginViewModelStateEvents.Event.GoToEmitScreen -> {}
        }
    }

    private fun fetchUserAttributes() {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                _state.update { currentState ->
                    currentState.copy(
                        success = attributes.firstOrNull {
                            it.key.keyString == "name"
                        }?.value.toString()
                    )
                }
            },
            { error ->
                _state.update { currentState ->
                    currentState.copy(
                        error = error.message
                    )
                }
            }
        )
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
        data object SignIn : Event
        data object SignOut : Event
        data object GoToEmitScreen : Event
    }

    sealed class Navigation {
        data object GoToEmitScreen : Navigation()
    }
}

