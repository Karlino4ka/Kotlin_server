package com.example.kotlin_kursach_server.config

import java.io.File
import java.util.Properties

object PublicUrlConfig {

    private val baseUrl: String by lazy { resolveBaseUrl() }

    fun toAbsoluteUrl(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return baseUrl.trimEnd('/') + normalizedPath
    }

    fun uploadsDirectory(): File {
        val dir = File("uploads")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun resolveBaseUrl(): String {
        System.getenv("PUBLIC_BASE_URL")?.takeIf { it.isNotBlank() }?.let { return it.trimEnd('/') }

        val fromFile = Properties().apply {
            findLocalPropertiesFile()?.inputStream()?.use { load(it) }
        }.getProperty("public.base.url")?.trim()

        return fromFile?.trimEnd('/') ?: "http://10.0.2.2:8080"
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
