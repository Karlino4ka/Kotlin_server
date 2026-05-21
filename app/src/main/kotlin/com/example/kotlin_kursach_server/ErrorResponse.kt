package com.example.kotlin_kursach_server

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
)
