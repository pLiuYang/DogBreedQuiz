plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
    id("kotlinx-serialization")
}

android {
    namespace = "com.dogbreedquiz.app"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.dogbreedquiz.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Add Hilt test runner for dependency injection in tests
        testInstrumentationRunner = "com.dogbreedquiz.app.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))

    // Core Android dependencies
    implementation(libs.bundles.androidx)
    implementation(libs.androidx.material)

    // Compose dependencies
    implementation(libs.bundles.compose)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3.window.size)
    implementation(libs.compose.material3.adaptive.navigation.suite)
    implementation(libs.compose.animation)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Image loading
    implementation(libs.coil.compose)

    // Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    // JSON serialization
    implementation(libs.kotlinx.serialization.json)

    // Networking
    implementation(libs.bundles.networking)
    implementation(libs.retrofit.kotlinx.serialization)

    // Dependency Injection
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Room testing
    testImplementation(libs.androidx.room.testing)

    // WorkManager for background cache operations
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Testing
    testImplementation(libs.bundles.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation("org.jetbrains.kotlin:kotlin-test:1.9.10")

    // Additional testing dependencies
    kspTest(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)

    // Debug
    debugImplementation(libs.bundles.compose.debug)
    testImplementation(kotlin("test"))
}