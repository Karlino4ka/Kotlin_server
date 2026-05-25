package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.admin.AdminConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

private const val USER_EMAIL_HEADER = "X-User-Email"

fun Application.configureRouting(repository: InstitutionRepository) {
    routing {
        get("/health") {
            call.respond(HealthResponse(status = "ok"))
        }

        get("/auth/is-admin") {
            val email = call.request.headers[USER_EMAIL_HEADER]
            call.respond(AdminStatusResponse(isAdmin = AdminConfig.isAdmin(email)))
        }

        route("/institutions") {
            get {
                call.respond(repository.getAll())
            }
            post {
                val email = call.request.headers[USER_EMAIL_HEADER]
                if (!AdminConfig.isAdmin(email)) {
                    val hint = when {
                        AdminConfig.adminCount() == 0 ->
                            "На сервере не задан admin.emails в local.properties"
                        email.isNullOrBlank() ->
                            "Заголовок X-User-Email не передан (войдите в аккаунт в приложении)"
                        else ->
                            "Email '$email' не в списке admin.emails на сервере"
                    }
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Доступ только для администратора. $hint"),
                    )
                    return@post
                }
                val request = call.receive<CreateInstitutionRequest>()
                if (request.name.isBlank() || request.city.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Название и город обязательны"),
                    )
                    return@post
                }
                val created = repository.create(request)
                call.respond(HttpStatusCode.Created, created)
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
