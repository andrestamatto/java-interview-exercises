plugins {
    base
    alias(libs.plugins.dependencyLicense)
    alias(libs.plugins.versions)
}

group = "io.github.andrestamatto"
version = "1.0.0-SNAPSHOT"

tasks.register("verifySolutions") {
    group = "verification"
    description = "Runs every implemented solution verification task."
    dependsOn(gradle.includedBuild("build-logic").task(":check"))
    dependsOn(subprojects.filter { it.name == "solution" }.map { "${it.path}:check" })
}

tasks.register("compileStarters") {
    group = "verification"
    description = "Compiles and runs the non-acceptance checks for every starter."
    dependsOn(subprojects.filter { it.name == "starter" }.map { "${it.path}:check" })
}

tasks.register("formatCheck") {
    group = "verification"
    description = "Checks formatting in every implemented Java module."
    dependsOn(subprojects.filter { it.name in setOf("starter", "solution") }
        .map { "${it.path}:spotlessCheck" })
}

tasks.register("verifyStarterIsolation") {
    group = "verification"
    description = "Fails if a starter can resolve or declare a dependency on a solution."
    doLast {
        val violations = subprojects
            .filter { it.name == "starter" }
            .flatMap { starter ->
                val declared = starter.configurations.flatMap { configuration ->
                    configuration.dependencies
                        .withType(org.gradle.api.artifacts.ProjectDependency::class.java)
                        .filter { it.path.contains(":solution") }
                        .map { "declared ${starter.path}:${configuration.name} -> ${it.path}" }
                }
                val resolved = starter.configurations
                    .filter { it.isCanBeResolved }
                    .flatMap { configuration ->
                        configuration.incoming.resolutionResult.allComponents
                            .filterIsInstance<org.gradle.api.artifacts.result.ResolvedComponentResult>()
                            .mapNotNull { component ->
                                val identifier = component.id
                                        as? org.gradle.api.artifacts.component.ProjectComponentIdentifier
                                identifier
                                    ?.takeIf { it.projectPath.contains(":solution") }
                                    ?.let {
                                        "resolved ${starter.path}:${configuration.name} -> ${it.projectPath}"
                                    }
                            }
                    }
                val buildScriptReference = starter.buildFile
                    .takeIf { it.isFile && it.readText().contains("solution", ignoreCase = true) }
                    ?.let { listOf("build script ${starter.path} references solution output") }
                    .orEmpty()
                declared + resolved + buildScriptReference
            }
        check(violations.isEmpty()) {
            "Starter-to-solution dependencies are forbidden:\n${violations.joinToString("\n")}"
        }
    }
}

tasks.register("verifyExercise01") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 01."
    dependsOn(
        ":exercises:01-lost-update-stock-reservation:starter:check",
        ":exercises:01-lost-update-stock-reservation:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise02") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 02."
    dependsOn(
        ":exercises:02-volatile-shutdown-visibility:starter:check",
        ":exercises:02-volatile-shutdown-visibility:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise03") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 03."
    dependsOn(
        ":exercises:03-atomic-quota-compound-operation:starter:check",
        ":exercises:03-atomic-quota-compound-operation:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise04") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 04."
    dependsOn(
        ":exercises:04-immutable-snapshot-safe-publication:starter:check",
        ":exercises:04-immutable-snapshot-safe-publication:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise05") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 05."
    dependsOn(
        ":exercises:05-deadlock-free-account-transfer:starter:check",
        ":exercises:05-deadlock-free-account-transfer:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise06") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 06."
    dependsOn(
        ":exercises:06-reduce-receipt-lock-contention:starter:check",
        ":exercises:06-reduce-receipt-lock-contention:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise07") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 07."
    dependsOn(
        ":exercises:07-bounded-import-executor:starter:check",
        ":exercises:07-bounded-import-executor:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise08") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 08."
    dependsOn(
        ":exercises:08-virtual-threads-downstream-bulkhead:starter:check",
        ":exercises:08-virtual-threads-downstream-bulkhead:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise09") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 09."
    dependsOn(
        ":exercises:09-completable-future-task-lifetime:starter:check",
        ":exercises:09-completable-future-task-lifetime:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.register("verifyExercise10") {
    group = "verification"
    description = "Compiles the starter and verifies the reference solution for exercise 10."
    dependsOn(
        ":exercises:10-async-request-context:starter:check",
        ":exercises:10-async-request-context:solution:check",
        "verifyStarterIsolation",
    )
}

tasks.named("check") {
    dependsOn("formatCheck", "compileStarters", "verifySolutions", "verifyStarterIsolation")
}
