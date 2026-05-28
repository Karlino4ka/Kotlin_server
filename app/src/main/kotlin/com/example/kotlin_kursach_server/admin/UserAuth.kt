package com.example.kotlin_kursach_server.admin

import com.example.kotlin_kursach_server.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

const val USER_EMAIL_HEADER = "X-User-Email"
const val USER_ID_HEADER = "X-User-Id"
const val USER_NAME_HEADER = "X-User-Name"

data class AuthenticatedUser(
    val userId: String,
    val email: String,
    val displayName: String?,
)

suspend fun ApplicationCall.requireAuthenticatedUserOrRespond(): AuthenticatedUser? {
    val userId = request.headers[USER_ID_HEADER]?.trim().orEmpty()
    val email = request.headers[USER_EMAIL_HEADER]?.trim().orEmpty()
    if (userId.isBlank() || email.isBlank()) {
        respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse("Войдите в аккаунт, чтобы оставить отзыв"),
        )
        return null
    }
    return AuthenticatedUser(
        userId = userId,
        email = email,
        displayName = request.headers[USER_NAME_HEADER]?.trim()?.takeIf { it.isNotEmpty() },
    )
}
