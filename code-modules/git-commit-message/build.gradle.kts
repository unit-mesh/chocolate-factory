@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    java
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.serialization.json)
    implementation(libs.logging.slf4j.api)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.test.junit.engine)
}
