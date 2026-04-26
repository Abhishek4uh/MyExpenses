plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.myexpenses"
    compileSdk {
        version = release(36){
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.myexpenses"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            applicationIdSuffix = ".release"
            versionNameSuffix = "-release"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    //Additional
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.gif)
    implementation(libs.timber)
    implementation(libs.splash.screen.api)
    implementation(libs.mhssn.colorpicker)
    implementation(libs.constraintlayout.compose)
    implementation(libs.androidx.datastore.preferences)

    //Navigation and Serialization
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)


    //Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    kspAndroidTest(libs.hilt.android.compiler)
    //To use hiltviewmodel() annotation
    implementation(libs.androidx.hilt.navigation.compose)

    //Material Icons
    implementation(libs.androidx.material.icons.extended)

    //Permissions
    implementation(libs.accompanist.permissions)

    //Ktor Bundle
    implementation(libs.bundles.ktor)

    //Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)


    //Auto OTP Retrieval
    implementation(libs.auth)

    //Map
    implementation(libs.map.compose)
    implementation(libs.map.widget)
    implementation(libs.map.utils)
    implementation(libs.okhttp)
    implementation(libs.java.websocket)
    implementation(libs.snappy)
    implementation(libs.places)

    //Lottie
    implementation(libs.lottie)
    implementation(libs.biometric)

    implementation(libs.retrofit)
    implementation(libs.retrofit.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    //Window Size
    implementation(libs.material.adaptive)
    implementation(libs.androidx.compose.material3)
}