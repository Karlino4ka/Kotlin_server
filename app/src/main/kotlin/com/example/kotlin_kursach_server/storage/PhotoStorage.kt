package com.example.kotlin_kursach_server.storage

import com.example.kotlin_kursach_server.config.PublicUrlConfig
import java.io.File
import java.util.UUID

object PhotoStorage {

    fun saveUploadedFile(institutionId: String, bytes: ByteArray, extension: String): String {
        val safeExtension = extension.lowercase().takeIf { it in ALLOWED_EXTENSIONS } ?: "jpg"
        val institutionDir = File(PublicUrlConfig.uploadsDirectory(), institutionId).apply { mkdirs() }
        val fileName = "${UUID.randomUUID()}.$safeExtension"
        File(institutionDir, fileName).writeBytes(bytes)
        return "/uploads/$institutionId/$fileName"
    }

    fun deleteByUrl(url: String) {
        val relative = toRelativeUploadPath(url) ?: return
        val file = File(PublicUrlConfig.uploadsDirectory(), relative)
        if (file.exists()) {
            file.delete()
        }
    }

    fun toRelativeUploadPath(url: String): String? {
        if (url.startsWith("/uploads/")) return url
        val pathIndex = url.indexOf("/uploads/")
        if (pathIndex >= 0) return url.substring(pathIndex)
        return null
    }

    private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
}
