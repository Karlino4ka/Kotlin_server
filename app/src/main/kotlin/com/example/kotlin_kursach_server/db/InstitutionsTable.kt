package com.example.kotlin_kursach_server.db

import org.jetbrains.exposed.sql.Table

object InstitutionsTable : Table("institutions") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val type = varchar("type", 32)
    val city = varchar("city", 128)
    val address = varchar("address", 512)
    val description = text("description")
    val phone = varchar("phone", 64).nullable()
    val website = varchar("website", 512).nullable()

    override val primaryKey = PrimaryKey(id)
}
