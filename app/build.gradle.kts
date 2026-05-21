plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.example.kotlin_kursach_server"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.example.kotlin_kursach_server.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    testImplementation(libs.junit)
}

tasks.named<Test>("test") {
    useJUnit()
}
