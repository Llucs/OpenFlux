plugins {
    id("com.android.library")
}

val compileSdk: String by project
val minSdk: String by project
val targetSdk: String by project

android {
    namespace = "com.termux.view"
    compileSdk = compileSdk.toInt()

    defaultConfig {
        minSdk = minSdk.toInt()
        targetSdk = targetSdk.toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    api(project(":termux:terminal-emulator"))
}
