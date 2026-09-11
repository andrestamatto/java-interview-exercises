plugins {
    id("solution-exercise")
}

val jcstress = sourceSets.create("jcstress") {
    java.srcDir("src/jcstress/java")
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add("jcstressImplementation", libs.jcstress)
    add("jcstressAnnotationProcessor", libs.jcstress)
}

tasks.register<JavaExec>("jcstress") {
    group = "verification"
    description = "Runs the optional Java Memory Model evidence suite."
    classpath = jcstress.runtimeClasspath
    mainClass = "org.openjdk.jcstress.Main"
    args("-v")
}

sourceSets.test {
    java.srcDir("../acceptance-tests/src/test/java")
}
