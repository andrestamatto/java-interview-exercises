plugins {
    id("java-exercise")
}

val java25 = sourceSets.create("java25") {
    java.srcDir("src/java25/java")
    compileClasspath += sourceSets.main.get().output + configurations.compileClasspath.get()
    runtimeClasspath += output + compileClasspath
}

val java25Compiler = javaToolchains.compilerFor {
    languageVersion = JavaLanguageVersion.of(25)
    vendor = JvmVendorSpec.ADOPTIUM
}

tasks.named<JavaCompile>(java25.compileJavaTaskName) {
    javaCompiler.set(java25Compiler)
    options.release.set(25)
}

tasks.register("compileJava25Extension") {
    group = "verification"
    description = "Compiles the isolated Java 25 extension without changing the Java 21 baseline."
    dependsOn(java25.classesTaskName)
}
