plugins { id("solution-exercise") }

val jmh = sourceSets.create("jmh") {
    java.srcDir("src/jmh/java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

sourceSets.test {
    java.srcDir("../acceptance-tests/src/test/java")
    compileClasspath += jmh.output
    runtimeClasspath += jmh.output
}

dependencies {
    add("jmhImplementation", libs.jmh.core)
    add("jmhAnnotationProcessor", libs.jmh.generator)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.jmh.core)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<JavaExec>("jmh") {
    group = "verification"
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    doFirst { layout.buildDirectory.dir("reports/jmh").get().asFile.mkdirs() }
    args("-rf", "json", "-rff", layout.buildDirectory.file("reports/jmh/results.json").get().asFile.path)
}

tasks.register<JavaExec>("jmhSmoke") {
    group = "verification"
    description = "Runs a short JMH smoke benchmark without score assertions."
    dependsOn(jmh.classesTaskName)
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    doFirst { layout.buildDirectory.dir("reports/jmh").get().asFile.mkdirs() }
    args("-wi", "1", "-i", "1", "-f", "1", "-w", "100ms", "-r", "100ms", "-rf", "json", "-rff", layout.buildDirectory.file("reports/jmh/smoke.json").get().asFile.path)
}
