plugins { id("starter-exercise") }
sourceSets.test { java.srcDir("../acceptance-tests/src/test/java") }
dependencies { testImplementation(platform(libs.junit.bom)); testImplementation(libs.junit.jupiter); testImplementation(libs.assertj); testRuntimeOnly("org.junit.platform:junit-platform-launcher") }
