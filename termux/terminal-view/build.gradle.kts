plugins {
    id("com.android.library")
}

val compileSdk = (project.properties["compileSdk"]?.toString() ?: "35").toInt()
val minSdk = (project.properties["minSdk"]?.toString() ?: "26").toInt()
val targetSdk = (project.properties["targetSdk"]?.toString() ?: "35").toInt()

android {
    namespace = "com.termux.view"
    compileSdk = compileSdk

    defaultConfig {
        minSdk = minSdk
        targetSdk = targetSdk
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
