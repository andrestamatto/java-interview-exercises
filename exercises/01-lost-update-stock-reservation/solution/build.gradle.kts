plugins {
    id("solution-exercise")
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.jqwik)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

sourceSets.test {
    java.srcDir("../acceptance-tests/src/test/java")
}
