package com.example.aws_cognito_test.di

import com.example.aws_cognito_test.presentation.screens.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        LoginViewModel()
    }
}
