plugins { id("solution-exercise") }

val jmh = sourceSets.create("jmh") {
    java.srcDir("src/jmh/java")
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add("jmhImplementation", libs.jmh.core)
    add("jmhAnnotationProcessor", libs.jmh.generator)
}

tasks.register<JavaExec>("jmh") {
    group = "verification"
    description = "Runs the manual JMH contention comparison. Results are not a correctness gate."
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    args("-rf", "json", "-rff", layout.buildDirectory.file("reports/jmh/results.json").get().asFile.path)
}

sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }
