plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appVersionName =
    project.findProperty("appVersionName")?.toString() ?: "1.0.0"

val appVersionCode =
    project.findProperty("appVersionCode")
        ?.toString()
        ?.toInt()
        ?: 1

val compileSdkVersion =
    project.findProperty("compileSdkVersion")
        ?.toString()
        ?.toInt()
        ?: 35

val minSdkVersion =
    project.findProperty("minSdkVersion")
        ?.toString()
        ?.toInt()
        ?: 26

val targetSdkVersion =
    project.findProperty("targetSdkVersion")
        ?.toString()
        ?.toInt()
        ?: 35

android {
    namespace = "com.openflux.app"

    compileSdk = compileSdkVersion

    defaultConfig {
        applicationId = "com.openflux.app"

        minSdk = minSdkVersion
        targetSdk = targetSdkVersion

        versionCode = appVersionCode
        versionName = appVersionName

        buildConfigField(
            "String",
            "VERSION_NAME",
            "\"$appVersionName\""
        )

        buildConfigField(
            "int",
            "VERSION_CODE",
            appVersionCode.toString()
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/NOTICE.md"
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {
    val composeBom =
        platform("androidx.compose:compose-bom:2024.12.01")

    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.activity:activity-compose:1.9.3")

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.8.7"
    )

    implementation(
        "androidx.navigation:navigation-compose:2.8.5"
    )

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.core:core-ktx:1.15.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    implementation("org.json:json:20240303")

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"
    )

    implementation(project(":termux:terminal-view"))
}