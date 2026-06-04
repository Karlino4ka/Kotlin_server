package com.example.kotlin_kursach_server

import kotlinx.serialization.Serializable

@Serializable
enum class InstitutionOrientation {
    TECHNICAL,
    HUMANITARIAN,
    MEDICAL,
}
