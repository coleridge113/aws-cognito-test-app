package com.example.aws_cognito_test.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import com.example.aws_cognito_test.data.datastore.dataStore
import com.example.aws_cognito_test.data.database.AppDatabase
import com.example.aws_cognito_test.data.datastore.AuthLocalDataSource
import com.example.aws_cognito_test.data.remote.api.AuthService
import com.example.aws_cognito_test.data.local.LocationRepositoryImpl
import com.example.aws_cognito_test.data.utils.LocalFileLoader
import com.example.aws_cognito_test.data.utils.OSLocationManager
import com.example.aws_cognito_test.domain.utils.TrackingManager
import com.example.aws_cognito_test.domain.utils.IotManager
import com.example.aws_cognito_test.domain.repository.LocationRepository
import com.example.aws_cognito_test.presentation.screens.emit.EmitViewModel
import com.example.aws_cognito_test.presentation.screens.login.LoginViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
    }
    single {
        get<Retrofit>().create(AuthService::class.java)
    }
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "location_db"
        ).build()
    }
    single<DataStore<Preferences>> {
        androidContext().dataStore 
    }
    single {
        AuthLocalDataSource(get())
    }
    single<LocationRepository>{
        LocationRepositoryImpl(get<AppDatabase>().locationDao())
    }
    single {
        TrackingManager()
    }
    single {
        LocalFileLoader(androidContext())
    }
    single {
        OSLocationManager(androidContext())
    }
    single {
        IotManager(androidContext())
    }
    viewModel {
        LoginViewModel()
    }
    viewModel {
        EmitViewModel(get(), get(), get(), get(), get())
    }
}
