plugins {
    id("starter-exercise")
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets.test {
    java.srcDir("../acceptance-tests/src/test/java")
}
