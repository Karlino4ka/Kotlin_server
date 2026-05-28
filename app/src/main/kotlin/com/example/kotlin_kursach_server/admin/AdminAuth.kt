package com.example.kotlin_kursach_server.admin

import com.example.kotlin_kursach_server.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

suspend fun ApplicationCall.requireAdminOrRespond(): Boolean {
    val email = request.headers[USER_EMAIL_HEADER]
    if (!AdminConfig.isAdmin(email)) {
        val hint = when {
            AdminConfig.adminCount() == 0 ->
                "На сервере не задан admin.emails в local.properties"
            email.isNullOrBlank() ->
                "Заголовок X-User-Email не передан (войдите в аккаунт в приложении)"
            else ->
                "Email '$email' не в списке admin.emails на сервере"
        }
        respond(
            HttpStatusCode.Forbidden,
            ErrorResponse("Доступ только для администратора. $hint"),
        )
        return false
    }
    return true
}
