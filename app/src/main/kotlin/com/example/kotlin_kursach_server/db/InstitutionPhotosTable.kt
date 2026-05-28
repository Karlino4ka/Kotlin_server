package com.example.kotlin_kursach_server.db

import org.jetbrains.exposed.sql.Table

object InstitutionPhotosTable : Table("institution_photos") {
    val id = varchar("id", 36)
    val institutionId = varchar("institution_id", 36)
    val url = varchar("url", 1024)
    val sortOrder = integer("sort_order").default(0)

    override val primaryKey = PrimaryKey(id)
}
