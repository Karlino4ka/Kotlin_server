package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.InstitutionsTable
import com.example.kotlin_kursach_server.db.toInstitution
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class InstitutionRepository {

    fun getAll(): List<Institution> = transaction {
        InstitutionsTable
            .selectAll()
            .orderBy(InstitutionsTable.name to SortOrder.ASC)
            .map { it.toInstitution() }
    }

    fun getById(id: String): Institution? = transaction {
        InstitutionsTable
            .selectAll()
            .where { InstitutionsTable.id eq id }
            .map { it.toInstitution() }
            .singleOrNull()
    }

    fun create(request: CreateInstitutionRequest): Institution = transaction {
        val id = UUID.randomUUID().toString()
        InstitutionsTable.insert {
            it[InstitutionsTable.id] = id
            it[name] = request.name.trim()
            it[type] = request.type.name
            it[city] = request.city.trim()
            it[address] = request.address.trim()
            it[description] = request.description.trim()
            it[phone] = request.phone?.trim()?.takeIf { p -> p.isNotEmpty() }
            it[website] = request.website?.trim()?.takeIf { w -> w.isNotEmpty() }
        }
        getById(id) ?: error("Failed to read created institution")
    }
}
