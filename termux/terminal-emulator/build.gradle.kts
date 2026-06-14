plugins {
    id("com.android.library")
}

val compileSdk: String by project
val minSdk: String by project
val targetSdk: String by project

android {
    namespace = "com.termux.terminal"
    compileSdk = compileSdk.toInt()

    defaultConfig {
        minSdk = minSdk.toInt()
        targetSdk = targetSdk.toInt()

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
