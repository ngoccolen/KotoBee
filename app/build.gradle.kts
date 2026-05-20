// File: KotoBee/app/build.gradle.kts (Module-level)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)

}
android {
    namespace = "com.example.kotobee"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }



    defaultConfig {
        applicationId = "com.example.kotobee"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Lõi Android & Compose ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.text)

    // --- Navigation ---
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.common.ktx)

    // --- Firebase & Play Services ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.annotations)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // --- ML Kit (Translate) ---
    implementation("com.google.mlkit:translate:17.0.2")
    implementation(libs.kotlinx.serialization.json)

    // --- Coroutines & Network ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

    // --- Load Ảnh / SVG (Coil) ---
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-svg:2.7.0")


    // --- Paging 3 (Phân trang) ---
    implementation("androidx.paging:paging-runtime")
    implementation("androidx.paging:paging-compose")
    implementation(libs.androidx.paging.common)

    // --- Animation ---
    implementation("androidx.compose.animation:animation")

    // --- Room Database (Đã dọn dẹp sạch sẽ) ---
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.6.1")
    implementation("com.google.code.gson:gson:2.10.1")
    add("ksp", "androidx.room:room-compiler:2.6.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Hilt

// Media3 / ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")

// Guava
    implementation("com.google.guava:guava:33.2.1-android")

// Image Cropper
    implementation("com.vanniktech:android-image-cropper:4.6.0")

// Gemini AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// Firebase Storage
    implementation(libs.firebase.storage)
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kotlin {
    jvmToolchain(17)
}
