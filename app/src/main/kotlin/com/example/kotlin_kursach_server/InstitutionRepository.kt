package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.InstitutionsTable
import com.example.kotlin_kursach_server.db.toInstitution
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

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
}
