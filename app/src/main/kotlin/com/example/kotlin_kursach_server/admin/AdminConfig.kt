package com.example.kotlin_kursach_server.admin

import org.slf4j.LoggerFactory
import java.io.File
import java.util.Properties

object AdminConfig {

    private val logger = LoggerFactory.getLogger(AdminConfig::class.java)
    private val adminEmails: Set<String> by lazy { loadAdminEmails() }

    fun isAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return adminEmails.contains(email.trim().lowercase())
    }

    fun adminCount(): Int = adminEmails.size

    private fun loadAdminEmails(): Set<String> {
        val fromEnv = System.getenv("ADMIN_EMAILS")
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()

        val fromFile = loadFromLocalProperties()

        val result = (fromEnv + fromFile).toSet()
        if (result.isEmpty()) {
            logger.warn(
                "Список admin.emails пуст! Добавьте admin.emails=email@example.com " +
                    "в Kotlin_Kursach_Server/local.properties или ADMIN_EMAILS в окружение.",
            )
        } else {
            logger.info("Загружено администраторов: {}", result.size)
        }
        return result
    }

    private fun loadFromLocalProperties(): List<String> {
        val file = findLocalPropertiesFile() ?: return emptyList()
        return Properties().apply {
            file.inputStream().use { load(it) }
        }.getProperty("admin.emails")
            ?.split(",")
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotEmpty() }
            .orEmpty()
    }

    private fun findLocalPropertiesFile(): File? {
        val userDir = System.getProperty("user.dir") ?: return null
        val candidates = listOf(
            File("local.properties"),
            File(userDir, "local.properties"),
            File(userDir).parentFile?.let { File(it, "local.properties") },
        ).filterNotNull()
        return candidates.firstOrNull { it.exists() }
    }
}
