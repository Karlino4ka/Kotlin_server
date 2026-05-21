package com.example.kotlin_kursach_server

import kotlinx.serialization.Serializable

@Serializable
data class Institution(
    val id: String,
    val name: String,
    val type: InstitutionType,
    val city: String,
    val address: String,
    val description: String,
    val phone: String? = null,
    val website: String? = null,
)
