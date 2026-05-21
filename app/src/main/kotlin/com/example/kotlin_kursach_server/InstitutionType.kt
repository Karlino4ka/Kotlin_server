package com.example.kotlin_kursach_server

import kotlinx.serialization.Serializable

@Serializable
enum class InstitutionType {
    SCHOOL,
    COLLEGE,
    UNIVERSITY,
}
