plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.manga_project"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.manga_project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Optimizaciones para desarrollo
        multiDexEnabled = true
    }

    buildTypes {
        debug {
            // Optimizaciones para desarrollo
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Optimizaciones de compilación
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.swiperefreshlayout)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.mpandroidchart)
    testImplementation(libs.junit)
    implementation(libs.picasso)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.play.services.auth)
    implementation (libs.glide)
    implementation(libs.photoview)
    implementation (libs.mpandroidchart)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation (libs.firebase.auth)
    implementation(libs.okhttp.v4120)
    implementation(libs.logging.interceptor)
    annotationProcessor (libs.compiler)
    implementation(libs.compose.wallet.button)
    implementation (libs.play.services.pay)
    implementation (libs.play.services.wallet)
    implementation (libs.stripe.android)
    implementation (libs.facebook.android.sdk)
}