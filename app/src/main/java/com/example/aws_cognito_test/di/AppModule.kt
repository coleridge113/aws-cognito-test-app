package com.example.aws_cognito_test.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.aws_cognito_test.data.database.AppDatabase
import com.example.aws_cognito_test.data.datastore.AuthLocalDataSource
import com.example.aws_cognito_test.data.datastore.IotLocalDataSource
import com.example.aws_cognito_test.data.datastore.dataStore
import com.example.aws_cognito_test.data.local.AuthRepositoryImpl
import com.example.aws_cognito_test.data.local.LocationRepositoryImpl
import com.example.aws_cognito_test.data.remote.api.AuthService
import com.example.aws_cognito_test.data.remote.source.AuthRemoteDataSource
import com.example.aws_cognito_test.data.utils.LocalFileLoader
import com.example.aws_cognito_test.data.utils.OSLocationManager
import com.example.aws_cognito_test.domain.repository.AuthRepository
import com.example.aws_cognito_test.domain.repository.LocationRepository
import com.example.aws_cognito_test.domain.usecase.FetchCertificatesUseCase
import com.example.aws_cognito_test.domain.usecase.GetCertificateKeysUseCase
import com.example.aws_cognito_test.domain.usecase.GetTokenUseCase
import com.example.aws_cognito_test.domain.utils.IotManager
import com.example.aws_cognito_test.domain.utils.TrackingManager
import com.example.aws_cognito_test.presentation.screens.emit.EmitViewModel
import com.example.aws_cognito_test.presentation.screens.login.LoginViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:4000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
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
    single { get<AppDatabase>().locationDao() }
    single<DataStore<Preferences>> {
        androidContext().dataStore 
    }
    singleOf(::AuthLocalDataSource)
    singleOf(::AuthRemoteDataSource)
    singleOf(::IotLocalDataSource)
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
    singleOf(::AuthRepositoryImpl) { bind<AuthRepository>() }
    singleOf(::TrackingManager)
    singleOf(::LocalFileLoader)
    singleOf(::OSLocationManager)
    singleOf(::IotManager)
    factoryOf(::FetchCertificatesUseCase)
    factoryOf(::GetCertificateKeysUseCase)
    factoryOf(::GetTokenUseCase)
    viewModelOf(::LoginViewModel)
    viewModelOf(::EmitViewModel)
}
