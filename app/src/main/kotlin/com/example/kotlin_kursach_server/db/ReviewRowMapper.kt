package com.example.kotlin_kursach_server.db

import com.example.kotlin_kursach_server.Review
import org.jetbrains.exposed.sql.ResultRow
fun ResultRow.toReview(): Review = Review(
    id = this[ReviewsTable.id],
    institutionId = this[ReviewsTable.institutionId],
    userId = this[ReviewsTable.userId],
    authorEmail = this[ReviewsTable.userEmail],
    authorName = this[ReviewsTable.authorName],
    rating = this[ReviewsTable.rating],
    text = this[ReviewsTable.text],
    createdAt = this[ReviewsTable.createdAt],
)
