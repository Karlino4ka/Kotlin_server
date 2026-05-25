package com.example.kotlin_kursach_server.db

import java.io.File
import java.net.URI
import java.util.Properties

object DatabaseConfig {

    fun resolveJdbcUrl(): String {
        System.getenv("JDBC_DATABASE_URL")?.takeIf { it.isNotBlank() }?.let { return normalize(it) }
        System.getenv("DATABASE_URL")?.takeIf { it.isNotBlank() }?.let { return normalize(it) }

        val localProperties = Properties().apply {
            val file = File("local.properties")
            if (file.exists()) {
                file.inputStream().use { load(it) }
            }
        }
        localProperties.getProperty("database.url")?.takeIf { it.isNotBlank() }?.let { return normalize(it) }

        error(
            "Не задан URL базы данных. Укажите JDBC_DATABASE_URL, DATABASE_URL " +
                "или database.url в local.properties (см. local.properties.example)",
        )
    }

    private fun normalize(url: String): String = when {
        url.startsWith("jdbc:") -> url
        url.startsWith("postgresql://") -> postgresUriToJdbc(url)
        else -> error("Неподдерживаемый формат URL: $url")
    }

    /**
     * postgresql://user:pass@host:5432/db?sslmode=require
     * → jdbc:postgresql://host:5432/db?user=user&password=pass&sslmode=require
     */
    private fun postgresUriToJdbc(uriString: String): String {
        val uri = URI(uriString)
        val userInfo = uri.userInfo?.split(":") ?: error("В DATABASE_URL отсутствуют user:password")
        val user = userInfo.getOrElse(0) { "" }
        val password = userInfo.getOrElse(1) { "" }
        val host = uri.host ?: error("В DATABASE_URL отсутствует host")
        val portPart = if (uri.port > 0) ":${uri.port}" else ""
        val path = uri.path.removePrefix("/")
        val query = uri.rawQuery?.let { "?$it" }.orEmpty()
        val extra = buildString {
            append(if (query.isEmpty()) "?" else "&")
            append("user=").append(user)
            append("&password=").append(password)
        }
        return "jdbc:postgresql://$host$portPart/$path$query$extra"
    }
}
