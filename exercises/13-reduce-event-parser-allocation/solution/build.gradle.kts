plugins { id("solution-exercise") }

val jmh = sourceSets.create("jmh") {
    java.srcDir("src/jmh/java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += output + compileClasspath
}

sourceSets.test {
    java.srcDir("../acceptance-tests/src/test/java")
}

dependencies {
    add("jmhImplementation", libs.jmh.core)
    add("jmhAnnotationProcessor", libs.jmh.generator)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<JavaExec>("jmh") {
    group = "verification"
    dependsOn(jmh.classesTaskName)
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    doFirst { layout.buildDirectory.dir("reports/jmh").get().asFile.mkdirs() }
    args("-prof", "gc", "-rf", "json", "-rff", layout.buildDirectory.file("reports/jmh/results.json").get().asFile.path)
}

tasks.register<JavaExec>("jmhSmoke") {
    group = "verification"
    dependsOn(jmh.classesTaskName)
    classpath = jmh.runtimeClasspath
    mainClass = "org.openjdk.jmh.Main"
    args("-wi", "1", "-i", "1", "-f", "1", "-w", "100ms", "-r", "100ms", "-prof", "gc")
}
