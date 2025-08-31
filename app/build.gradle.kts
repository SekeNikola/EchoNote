// ...existing code...
kapt {
    correctErrorTypes = true
}


plugins {
    id("com.android.application") version "8.12.2"
        kotlin("android") version "1.9.23"
        id("org.jetbrains.kotlin.kapt") version "1.9.23"
}

repositories {
    google()
    mavenCentral()
}

android {
    namespace = "com.example.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.8.2")
        implementation("androidx.compose.ui:ui:1.6.7")
        implementation("androidx.compose.foundation:foundation:1.6.7") // Added this line
        implementation("androidx.compose.material3:material3:1.2.1")
        implementation("androidx.compose.ui:ui-tooling-preview:1.6.7")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
        implementation("androidx.navigation:navigation-compose:2.7.7")
        implementation("androidx.compose.material:material-icons-extended:1.6.7")
        implementation("androidx.compose.runtime:runtime-livedata:1.6.7")
        debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")
        debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
        androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Accompanist (optional for permissions, system UI)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Testing (optional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.4")
}

// ...existing code...