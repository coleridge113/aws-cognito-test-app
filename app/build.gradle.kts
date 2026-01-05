import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = File(rootDir, "local.properties").inputStream().use {
    Properties().apply { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.aws_cognito_test"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.aws_cognito_test"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "AWS_IOT_ENDPOINT",
            "\"${localProps["AWS_IOT_ENDPOINT"]}\""
        )
        buildConfigField(
            "String",
            "AWS_REGION",
            "\"${localProps["AWS_REGION"]}\""
        )
        buildConfigField(
            "String",
            "AWS_ACCESS_KEY_ID",
            "\"${localProps["AWS_ACCESS_KEY_ID"]}\""
        )
        buildConfigField(
            "String",
            "AWS_SECRET_ACCESS_KEY",
            "\"${localProps["AWS_SECRET_ACCESS_KEY"]}\""
        )
        buildConfigField(
            "String",
            "AWS_KEY_ID",
            "\"${localProps["AWS_KEY_ID"]}\""
        )
        buildConfigField(
            "String",
            "DEVICE_ID",
            "\"${localProps["DEVICE_ID"]}\""
        )
        buildConfigField(
            "String",
            "AWS_COGNITO_POOL_ID",
            "\"${localProps["AWS_COGNITO_POOL_ID"]}\""
        )
        buildConfigField(
            "String",
            "AWS_API_KEY",
            "\"${localProps["AWS_API_KEY"]}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        isCoreLibraryDesugaringEnabled = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // AWS Amplify Authenticator
    implementation("com.amplifyframework.ui:authenticator:1.8.0")
    implementation("com.amplifyframework:core:2.31.0")
    implementation("com.amplifyframework:aws-geo-location:2.31.0")

    // AWS Kotlin SDK
    implementation("software.amazon.location:tracking:1.0.1")
    // implementation("software.amazon.location:auth:1.1.1")
    // implementation("aws.sdk.kotlin:location:1.5.113")

    // Koin
    implementation("io.insert-koin:koin-android:3.5.6") 
    implementation("io.insert-koin:koin-androidx-compose:3.5.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") 
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

}
