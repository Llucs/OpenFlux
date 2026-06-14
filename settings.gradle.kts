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
include(":Termux:terminal-emulator")
include(":Termux:terminal-view")
project(":Termux:terminal-emulator").projectDir = file("Termux/terminal-emulator")
project(":Termux:terminal-view").projectDir = file("Termux/terminal-view")
