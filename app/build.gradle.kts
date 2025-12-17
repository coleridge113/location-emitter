import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

val localProps = File(rootDir, "local.properties").inputStream().use {
    Properties().apply { load(it) }
}

android {
    namespace = "com.luna.location_emitter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.luna.location_emitter"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["appAuthRedirectScheme"] = "com.metromart.locationemitter"

        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${localProps["MAPBOX_ACCESS_TOKEN"]}\""
        )
        buildConfigField(
            "String",
            "MAPBOX_DOWNLOADS_TOKEN",
            "\"${localProps["MAPBOX_DOWNLOADS_TOKEN"]}\""
        )
        buildConfigField(
            "String",
            "ABLY_API_KEY",
            "\"${localProps["ABLY_API_KEY"]}\""
        )
        buildConfigField(
            "String",
            "PUSHER_APP_ID",
            "\"${localProps["PUSHER_APP_ID"]}\""
        )
        buildConfigField(
            "String",
            "PUSHER_KEY",
            "\"${localProps["PUSHER_KEY"]}\""
        )
        buildConfigField(
            "String",
            "PUSHER_SECRET",
            "\"${localProps["PUSHER_SECRET"]}\""
        )
        buildConfigField(
            "String",
            "PUSHER_API_KEY",
            "\"${localProps["PUSHER_API_KEY"]}\""
        )
        buildConfigField(
            "String",
            "PUSHER_CLUSTER",
            "\"${localProps["PUSHER_CLUSTER"]}\""
        )
        buildConfigField(
            "String",
            "RADAR_TEST_SECRET",
            "\"${localProps["RADAR_TEST_SECRET"]}\""
        )
        buildConfigField(
            "String",
            "RADAR_TEST_PUBLISHABLE",
            "\"${localProps["RADAR_TEST_PUBLISHABLE"]}\""
        )
        buildConfigField(
            "String",
            "AWS_IOT_ENDPOINT",
            "\"${localProps["AWS_IOT_ENDPOINT"]}\""
        )
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
    }

    flavorDimensions += "environment"
    productFlavors {
        create("emulator") {
            isDefault = true
            dimension = "environment"
            buildConfigField(
                "String",
                "PUSHER_AUTH_BASE_URL",
                "\"http://10.0.2.2:3000\""
            )
        }
        create("device") {
            dimension = "environment"
            buildConfigField(
                "String",
                "PUSHER_AUTH_BASE_URL",
                "\"http://192.168.100.70:3000\""
            )
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
        isCoreLibraryDesugaringEnabled = true
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

    packaging {
        resources {
            excludes += listOf(
                "META-INF/INDEX.LIST", 
                "META-INF/io.netty.versions.properties",
                "META-INF/DEPENDENCIES"
            )
        }
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
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Coroutines 1.10.2
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Ably
    implementation(libs.ably.java)

    // Pusher
    implementation(libs.pusher.java.client)

    // Radar
    implementation(libs.sdk)

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Google Play Services Location
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.location)

    // MapLibre
    implementation(libs.android.sdk )
    implementation(libs.android.plugin.annotation.v9)

    // AWS
    implementation(libs.appauth)
    api(libs.aws.iot.device.sdk.android)
    implementation(libs.aws.android.sdk.core)
    implementation(libs.aws.android.sdk.cognito)
    implementation(libs.cognitoidentity)
    implementation(libs.auth)
    implementation(libs.org.eclipse.paho.client.mqttv3)
}
