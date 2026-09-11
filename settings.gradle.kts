pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "java-interview-exercises"

include(
    ":exercises:01-lost-update-stock-reservation:starter",
    ":exercises:01-lost-update-stock-reservation:solution",
)
