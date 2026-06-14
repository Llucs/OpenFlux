pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "OpenFlux"
include(":app")
include(":termux:terminal-emulator")
include(":termux:terminal-view")
project(":termux:terminal-emulator").projectDir = file("termux/terminal-emulator")
project(":termux:terminal-view").projectDir = file("termux/terminal-view")
