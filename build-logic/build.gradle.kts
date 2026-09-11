plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
}

dependencies {
    implementation("com.diffplug.spotless:com.diffplug.spotless.gradle.plugin:8.10.2")
}
