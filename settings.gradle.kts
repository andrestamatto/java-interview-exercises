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
    ":exercises:08-virtual-threads-downstream-bulkhead:starter",
    ":exercises:08-virtual-threads-downstream-bulkhead:solution",
    ":exercises:09-completable-future-task-lifetime:starter",
    ":exercises:09-completable-future-task-lifetime:solution",
    ":exercises:10-async-request-context:starter",
    ":exercises:10-async-request-context:solution",
    ":exercises:11-index-membership-lookups:starter",
    ":exercises:11-index-membership-lookups:solution",
    ":exercises:12-trustworthy-jmh-benchmark:starter",
    ":exercises:12-trustworthy-jmh-benchmark:solution",
    ":exercises:13-reduce-event-parser-allocation:starter",
    ":exercises:13-reduce-event-parser-allocation:solution",
)
