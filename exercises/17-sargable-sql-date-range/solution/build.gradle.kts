plugins {
    id("spring-exercise")
    id("integration-test")
}

sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }

dependencies {
    implementation("org.springframework:spring-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add("integrationTestImplementation", "org.springframework.boot:spring-boot-starter-test")
    add("integrationTestImplementation", "org.springframework.boot:spring-boot-data-jdbc-test")
    add("integrationTestImplementation", libs.testcontainers.junit.jupiter)
    add("integrationTestImplementation", libs.testcontainers.postgresql)
}
