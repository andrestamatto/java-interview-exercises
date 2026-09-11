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
    ":exercises:02-volatile-shutdown-visibility:starter",
    ":exercises:02-volatile-shutdown-visibility:solution",
    ":exercises:03-atomic-quota-compound-operation:starter",
    ":exercises:03-atomic-quota-compound-operation:solution",
    ":exercises:04-immutable-snapshot-safe-publication:starter",
    ":exercises:04-immutable-snapshot-safe-publication:solution",
    ":exercises:05-deadlock-free-account-transfer:starter",
    ":exercises:05-deadlock-free-account-transfer:solution",
    ":exercises:06-reduce-receipt-lock-contention:starter",
    ":exercises:06-reduce-receipt-lock-contention:solution",
    ":exercises:07-bounded-import-executor:starter",
    ":exercises:07-bounded-import-executor:solution",
)
