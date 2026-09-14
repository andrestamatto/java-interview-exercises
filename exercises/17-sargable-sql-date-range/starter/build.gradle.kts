plugins {
    id("spring-exercise")
    id("starter-exercise")
}

sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }

dependencies {
    implementation("org.springframework:spring-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
