package com.example.kotlin_kursach_server

import kotlinx.serialization.Serializable

@Serializable
data class InstitutionPhoto(
    val id: String,
    val url: String,
)

@Serializable
data class AddPhotoUrlRequest(
    val url: String,
)
