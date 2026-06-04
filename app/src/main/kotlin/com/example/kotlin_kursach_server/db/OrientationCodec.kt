package com.example.kotlin_kursach_server.db

import com.example.kotlin_kursach_server.InstitutionOrientation

fun List<InstitutionOrientation>.toStorageString(): String =
    distinct().joinToString(",") { it.name }

fun String.toOrientations(): List<InstitutionOrientation> {
    if (isBlank()) return emptyList()
    return split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { InstitutionOrientation.valueOf(it) }
}
