plugins {
    id("java-exercise")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("acceptance")
    }
}

tasks.register<Test>("starterAcceptanceTest") {
    group = "verification"
    description = "Runs the intentionally failing learner acceptance tests."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("acceptance")
    }
    shouldRunAfter(tasks.test)
}
