plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
}

allprojects {
    afterEvaluate {
        // Apply SDK versions to Termux subprojects that read from project.properties
        if (name.contains("Termux") || name.contains("terminal")) {
            project.ext.set("compileSdkVersion", "35")
            project.ext.set("minSdkVersion", "26")
            project.ext.set("targetSdkVersion", "35")
        }
    }
}
