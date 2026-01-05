package com.example.aws_cognito_test.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import com.example.aws_cognito_test.data.database.AppDatabase
import com.example.aws_cognito_test.data.local.LocationRepositoryImpl
import com.example.aws_cognito_test.domain.utils.TrackingManager
import com.example.aws_cognito_test.domain.repository.LocationRepository
import com.example.aws_cognito_test.presentation.screens.emit.EmitViewModel
import com.example.aws_cognito_test.presentation.screens.login.LoginViewModel

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "location_db"
        ).build()
    }
    single<LocationRepository>{
        LocationRepositoryImpl(get<AppDatabase>().locationDao())
    }
    single {
        TrackingManager()
    }
    viewModel {
        LoginViewModel()
    }
    viewModel {
        EmitViewModel(get())
    }
}
