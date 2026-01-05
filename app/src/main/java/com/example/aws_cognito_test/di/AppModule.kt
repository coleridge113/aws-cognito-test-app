package com.example.aws_cognito_test.di

import com.example.aws_cognito_test.domain.utils.TrackingManager
import com.example.aws_cognito_test.presentation.screens.emit.EmitViewModel
import com.example.aws_cognito_test.presentation.screens.login.LoginViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        TrackingManager()
    }
    viewModel {
        LoginViewModel()
    }
    viewModel {
        EmitViewModel(androidApplication(), get())
    }
}
