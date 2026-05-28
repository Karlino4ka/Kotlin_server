package com.example.kotlin_kursach_server.db

import org.jetbrains.exposed.sql.Table

object ReviewsTable : Table("reviews") {
    val id = varchar("id", 36)
    val institutionId = varchar("institution_id", 36)
    val userId = varchar("user_id", 128)
    val userEmail = varchar("user_email", 255)
    val authorName = varchar("author_name", 255).nullable()
    val rating = integer("rating")
    val text = text("text")
    val createdAt = varchar("created_at", 32)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(institutionId, userId)
    }
}
