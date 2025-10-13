pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "coroutine-dispatchers"

include(
    ":dispatchers",
    ":dispatchers-lint",
    ":dispatchers-test",
)
