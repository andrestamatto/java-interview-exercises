plugins {
    id("java-exercise")
    id("integration-test")
}

sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }
sourceSets.named("integrationTest") { java.srcDir("../acceptance-tests/src/integrationTest/java") }

dependencies {
    implementation(platform(libs.aws.sdk.bom))
    implementation(libs.aws.sdk.dynamodb)
    implementation(libs.aws.sdk.url.connection.client)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add("integrationTestImplementation", platform(libs.junit.bom))
    add("integrationTestImplementation", libs.junit.jupiter)
    add("integrationTestImplementation", libs.assertj)
    add("integrationTestImplementation", libs.testcontainers.core)
    add("integrationTestImplementation", libs.testcontainers.junit.jupiter)
}
