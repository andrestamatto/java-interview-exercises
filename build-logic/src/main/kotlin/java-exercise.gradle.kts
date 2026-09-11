plugins {
    `java-library`
    checkstyle
    id("com.diffplug.spotless")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

dependencyLocking {
    lockAllConfigurations()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:all")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "256m"
    jvmArgs("-XX:MaxMetaspaceSize=128m")
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

checkstyle {
    toolVersion = "11.0.1"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    maxWarnings = 0
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.28.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
