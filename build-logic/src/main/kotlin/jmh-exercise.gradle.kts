plugins {
    id("solution-exercise")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val jmh = sourceSets.create("jmh") {
    java.srcDir("src/jmh/java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

dependencies {
    add("jmhImplementation", catalog.findLibrary("jmh-core").get())
    add("jmhAnnotationProcessor", catalog.findLibrary("jmh-generator").get())
}

tasks.register<JavaExec>("jmh") {
    group = "verification"
    description = "Runs the full JMH suite and writes JSON results."
    dependsOn(jmh.classesTaskName)
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    doFirst { layout.buildDirectory.dir("reports/jmh").get().asFile.mkdirs() }
    args(
        "-rf",
        "json",
        "-rff",
        layout.buildDirectory.file("reports/jmh/results.json").get().asFile.path,
    )
}

tasks.register<JavaExec>("jmhSmoke") {
    group = "verification"
    description = "Runs a short JMH smoke benchmark without score assertions."
    dependsOn(jmh.classesTaskName)
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    doFirst { layout.buildDirectory.dir("reports/jmh").get().asFile.mkdirs() }
    args(
        "-wi",
        "1",
        "-i",
        "1",
        "-f",
        "1",
        "-w",
        "100ms",
        "-r",
        "100ms",
        "-prof",
        "gc",
        "-rf",
        "json",
        "-rff",
        layout.buildDirectory.file("reports/jmh/smoke.json").get().asFile.path,
    )
}
