import java.util.Properties

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
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    testImplementation(libs.h2)
    testImplementation(libs.junit)
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.1.3")
}

tasks.named<Test>("test") {
    useJUnit()
    environment("ADMIN_EMAILS", "admin@test.com")
}

tasks.named<JavaExec>("run") {
    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }
    localProperties.getProperty("database.url")?.let { url ->
        environment("JDBC_DATABASE_URL", url)
    }
    localProperties.getProperty("admin.emails")?.let { emails ->
        environment("ADMIN_EMAILS", emails)
    }
    localProperties.getProperty("public.base.url")?.let { url ->
        environment("PUBLIC_BASE_URL", url)
    }
}
