package com.example.kotlin_kursach_server.db

import com.example.kotlin_kursach_server.Institution
import com.example.kotlin_kursach_server.InstitutionType
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInstitution(): Institution = Institution(
    id = this[InstitutionsTable.id],
    name = this[InstitutionsTable.name],
    type = InstitutionType.valueOf(this[InstitutionsTable.type]),
    city = this[InstitutionsTable.city],
    address = this[InstitutionsTable.address],
    description = this[InstitutionsTable.description],
    phone = this[InstitutionsTable.phone],
    website = this[InstitutionsTable.website],
)
