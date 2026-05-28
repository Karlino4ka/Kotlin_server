package com.example.kotlin_kursach_server

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val institutionId: String,
    val userId: String,
    val authorEmail: String,
    val authorName: String? = null,
    val rating: Int,
    val text: String,
    val createdAt: String,
)

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val text: String,
)

@Serializable
data class InstitutionRatingStats(
    val averageRating: Double? = null,
    val reviewCount: Int = 0,
)
