package com.example.aws_cognito_test.presentation.screens.login

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.amplifyframework.ui.authenticator.ui.Authenticator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.example.aws_cognito_test.presentation.Routes
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    navController: NavController
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val onEvent = remember(viewModel) { viewModel::onEvent }

    LaunchedEffect(viewModel.navigation, lifecycleOwner) {
        viewModel.navigation.flowWithLifecycle(lifecycleOwner.lifecycle)
            .collectLatest { navigation ->
                when (navigation) {
                    LoginViewModelStateEvents.Navigation.GoToEmitScreen -> {
                        navController.navigate(Routes.EmitRoute) 
                    }
                }
            }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        MainContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onEvent = onEvent
        )
   }
}

@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    uiState: LoginViewModelStateEvents.UiState,
    onEvent: (LoginViewModelStateEvents.Event) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center 
    ) {
        Authenticator { state ->
            if (state.user.username.isNotEmpty()) {
                onEvent(LoginViewModelStateEvents.Event.FetchAttributes)
                val name = uiState.success
                Text(text = "Hello $name")
            }
        }
    }
}
