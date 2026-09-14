plugins {
    id("java-exercise")
    id("starter-exercise")
}

sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }

dependencies {
    implementation(platform(libs.aws.sdk.bom))
    implementation(libs.aws.sdk.dynamodb)
    implementation(libs.aws.sdk.url.connection.client)
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
