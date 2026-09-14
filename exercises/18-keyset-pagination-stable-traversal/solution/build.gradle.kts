plugins {
    id("spring-exercise")
    id("integration-test")
}

sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }
sourceSets.named("integrationTest") { java.srcDir("../acceptance-tests/src/integrationTest/java") }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add("integrationTestImplementation", "org.springframework.boot:spring-boot-starter-test")
    add("integrationTestImplementation", "org.springframework.boot:spring-boot-jdbc-test")
    add("integrationTestImplementation", libs.testcontainers.junit.jupiter)
    add("integrationTestImplementation", libs.testcontainers.postgresql)
}
