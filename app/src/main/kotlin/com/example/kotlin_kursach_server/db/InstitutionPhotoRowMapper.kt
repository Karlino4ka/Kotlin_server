package com.example.kotlin_kursach_server.db

import com.example.kotlin_kursach_server.InstitutionPhoto
import com.example.kotlin_kursach_server.config.PublicUrlConfig
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInstitutionPhoto(): InstitutionPhoto {
    val storedUrl = this[InstitutionPhotosTable.url]
    val publicUrl = if (storedUrl.startsWith("http://") || storedUrl.startsWith("https://")) {
        storedUrl
    } else {
        PublicUrlConfig.toAbsoluteUrl(storedUrl)
    }
    return InstitutionPhoto(
        id = this[InstitutionPhotosTable.id],
        url = publicUrl,
    )
}
