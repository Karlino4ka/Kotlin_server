package com.example.kotlin_kursach_server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(repository: InstitutionRepository) {
    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        route("/institutions") {
            get {
                call.respond(repository.getAll())
            }
            get("/{id}") {
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                    return@get
                }
                val institution = repository.getById(id)
                if (institution == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                } else {
                    call.respond(institution)
                }
            }
        }
    }
}
