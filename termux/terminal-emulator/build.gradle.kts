plugins {
    id("com.android.library")
}

val compileSdk = (project.properties["compileSdk"]?.toString() ?: "35").toInt()
val minSdk = (project.properties["minSdk"]?.toString() ?: "26").toInt()
val targetSdk = (project.properties["targetSdk"]?.toString() ?: "35").toInt()

android {
    namespace = "com.termux.terminal"
    compileSdk = compileSdk

    defaultConfig {
        minSdk = minSdk
        targetSdk = targetSdk

        externalNativeBuild {
            ndkBuild {
                cFlags += listOf("-std=c11", "-Wall", "-Wextra", "-Werror", "-Os", "-fno-stack-protector", "-Wl,--gc-sections")
            }
        }

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
