plugins {
    id("java-exercise")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(platform(catalog.findLibrary("spring-boot-dependencies").get()))
    testImplementation(platform(catalog.findLibrary("spring-boot-dependencies").get()))
}
