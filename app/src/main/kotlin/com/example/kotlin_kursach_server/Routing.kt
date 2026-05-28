package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.admin.AdminConfig
import com.example.kotlin_kursach_server.admin.USER_EMAIL_HEADER
import com.example.kotlin_kursach_server.admin.requireAdminOrRespond
import com.example.kotlin_kursach_server.admin.requireAuthenticatedUserOrRespond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    institutionRepository: InstitutionRepository,
    reviewRepository: ReviewRepository,
    photoRepository: InstitutionPhotoRepository,
) {
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
                call.respond(institutionRepository.getAll())
            }
            post {
                if (!call.requireAdminOrRespond()) return@post
                val request = call.receive<CreateInstitutionRequest>()
                val validationError = validateInstitutionRequest(request)
                if (validationError != null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(validationError))
                    return@post
                }
                val created = institutionRepository.create(request)
                call.respond(HttpStatusCode.Created, created)
            }
            route("/{id}") {
                get {
                    val id = call.parameters["id"]
                    if (id.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                        return@get
                    }
                    val institution = institutionRepository.getById(id)
                    if (institution == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                    } else {
                        call.respond(institution)
                    }
                }
                put {
                    if (!call.requireAdminOrRespond()) return@put
                    val id = call.parameters["id"]
                    if (id.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                        return@put
                    }
                    val request = call.receive<CreateInstitutionRequest>()
                    val validationError = validateInstitutionRequest(request)
                    if (validationError != null) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(validationError))
                        return@put
                    }
                    val updated = institutionRepository.update(id, request)
                    if (updated == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                    } else {
                        call.respond(updated)
                    }
                }
                delete {
                    if (!call.requireAdminOrRespond()) return@delete
                    val id = call.parameters["id"]
                    if (id.isNullOrBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                        return@delete
                    }
                    if (institutionRepository.delete(id)) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                    }
                }
                route("/photos") {
                    post {
                        if (!call.requireAdminOrRespond()) return@post
                        val id = call.parameters["id"]
                        if (id.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                            return@post
                        }
                        if (institutionRepository.getById(id) == null) {
                            call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                            return@post
                        }
                        var fileBytes: ByteArray? = null
                        var extension = "jpg"
                        call.receiveMultipart().forEachPart { part ->
                            when (part) {
                                is PartData.FileItem -> {
                                    fileBytes = part.streamProvider().readBytes()
                                    extension = part.originalFileName
                                        ?.substringAfterLast('.', "jpg")
                                        ?.lowercase()
                                        ?: "jpg"
                                }
                                else -> Unit
                            }
                            part.dispose()
                        }
                        if (fileBytes == null) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Файл не передан"))
                            return@post
                        }
                        val photo = photoRepository.addUploadedFile(id, fileBytes!!, extension)
                        call.respond(HttpStatusCode.Created, photo)
                    }
                    post("/url") {
                        if (!call.requireAdminOrRespond()) return@post
                        val id = call.parameters["id"]
                        if (id.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                            return@post
                        }
                        if (institutionRepository.getById(id) == null) {
                            call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                            return@post
                        }
                        val request = call.receive<AddPhotoUrlRequest>()
                        val validationError = validatePhotoUrl(request.url)
                        if (validationError != null) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(validationError))
                            return@post
                        }
                        val photo = photoRepository.addUrl(id, request.url)
                        call.respond(HttpStatusCode.Created, photo)
                    }
                    delete("/{photoId}") {
                        if (!call.requireAdminOrRespond()) return@delete
                        val id = call.parameters["id"]
                        val photoId = call.parameters["photoId"]
                        if (id.isNullOrBlank() || photoId.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                            return@delete
                        }
                        if (photoRepository.delete(id, photoId)) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(HttpStatusCode.NotFound, ErrorResponse("Photo not found"))
                        }
                    }
                }
                route("/reviews") {
                    get {
                        val id = call.parameters["id"]
                        if (id.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                            return@get
                        }
                        if (institutionRepository.getById(id) == null) {
                            call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                            return@get
                        }
                        call.respond(reviewRepository.getByInstitution(id))
                    }
                    post {
                        val id = call.parameters["id"]
                        if (id.isNullOrBlank()) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse("id is required"))
                            return@post
                        }
                        if (institutionRepository.getById(id) == null) {
                            call.respond(HttpStatusCode.NotFound, ErrorResponse("Institution not found"))
                            return@post
                        }
                        val user = call.requireAuthenticatedUserOrRespond() ?: return@post
                        val request = call.receive<CreateReviewRequest>()
                        val validationError = validateReviewRequest(request)
                        if (validationError != null) {
                            call.respond(HttpStatusCode.BadRequest, ErrorResponse(validationError))
                            return@post
                        }
                        val review = reviewRepository.createOrUpdate(
                            institutionId = id,
                            userId = user.userId,
                            userEmail = user.email,
                            authorName = user.displayName,
                            request = request,
                        )
                        call.respond(HttpStatusCode.Created, review)
                    }
                }
            }
        }
    }
}

private fun validateInstitutionRequest(request: CreateInstitutionRequest): String? {
    if (request.name.isBlank() || request.city.isBlank()) {
        return "Название и город обязательны"
    }
    return null
}

private fun validatePhotoUrl(url: String): String? {
    val trimmed = url.trim()
    if (trimmed.isBlank()) return "URL фотографии обязателен"
    if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
        return "URL должен начинаться с http:// или https://"
    }
    return null
}

private fun validateReviewRequest(request: CreateReviewRequest): String? {
    if (request.rating !in 1..5) {
        return "Оценка должна быть от 1 до 5"
    }
    if (request.text.isBlank()) {
        return "Текст отзыва обязателен"
    }
    if (request.text.length > 2000) {
        return "Отзыв слишком длинный (максимум 2000 символов)"
    }
    return null
}
