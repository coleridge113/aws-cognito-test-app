package com.example.aws_cognito_test.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.koin.androidx.compose.koinViewModel
import com.example.aws_cognito_test.presentation.screens.login.LoginScreen
import com.example.aws_cognito_test.presentation.screens.login.LoginViewModel
import com.example.aws_cognito_test.presentation.screens.EmitScreen
import com.example.aws_cognito_test.presentation.screens.emit.EmitViewModel

@ExperimentalMaterial3Api
@ExperimentalSharedTransitionApi
@Composable
fun NavGraphSetup(
    navController: NavHostController,
) {
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Routes.LoginRoute,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            composable<Routes.LoginRoute> {
                val viewModel = koinViewModel<LoginViewModel>()
                LoginScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }

            composable<Routes.EmitRoute> {
                val viewModel = koinViewModel<EmitViewModel>()
                EmitScreen(
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}
