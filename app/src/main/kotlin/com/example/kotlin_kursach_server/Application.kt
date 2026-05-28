package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.config.PublicUrlConfig
import com.example.kotlin_kursach_server.db.DatabaseConfig
import com.example.kotlin_kursach_server.db.DatabaseFactory
import com.example.kotlin_kursach_server.db.InstitutionSeeder
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val jdbcUrl = DatabaseConfig.resolveJdbcUrl()
    DatabaseFactory.init(jdbcUrl)
    DatabaseFactory.createTables()
    InstitutionSeeder.seedIfEmpty()
    Runtime.getRuntime().addShutdownHook(Thread { DatabaseFactory.close() })

    val reviewRepository = ReviewRepository()
    val photoRepository = InstitutionPhotoRepository()
    val institutionRepository = InstitutionRepository(reviewRepository, photoRepository)

    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }

    routing {
        staticFiles("/uploads", PublicUrlConfig.uploadsDirectory())
    }

    configureRouting(institutionRepository, reviewRepository, photoRepository)
}
