package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.InstitutionsTable
import com.example.kotlin_kursach_server.db.toInstitution
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class InstitutionRepository(
    private val reviewRepository: ReviewRepository,
    private val photoRepository: InstitutionPhotoRepository,
) {

    fun getAll(): List<Institution> {
        val stats = reviewRepository.ratingStatsByInstitution()
        val photos = photoRepository.photosByInstitution()
        return transaction {
            InstitutionsTable
                .selectAll()
                .orderBy(InstitutionsTable.name to SortOrder.ASC)
                .map { row ->
                    val base = row.toInstitution()
                    base.enrich(
                        stats = stats[base.id],
                        photos = photos[base.id].orEmpty(),
                    )
                }
        }
    }

    fun getById(id: String): Institution? = transaction {
        val base = InstitutionsTable
            .selectAll()
            .where { InstitutionsTable.id eq id }
            .map { it.toInstitution() }
            .singleOrNull() ?: return@transaction null
        base.enrich(
            stats = reviewRepository.statsForInstitution(id),
            photos = photoRepository.getByInstitution(id),
        )
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

    fun update(id: String, request: CreateInstitutionRequest): Institution? = transaction {
        val updated = InstitutionsTable.update({ InstitutionsTable.id eq id }) {
            it[name] = request.name.trim()
            it[type] = request.type.name
            it[city] = request.city.trim()
            it[address] = request.address.trim()
            it[description] = request.description.trim()
            it[phone] = request.phone?.trim()?.takeIf { p -> p.isNotEmpty() }
            it[website] = request.website?.trim()?.takeIf { w -> w.isNotEmpty() }
        }
        if (updated == 0) null else getById(id)
    }

    fun delete(id: String): Boolean = transaction {
        reviewRepository.deleteByInstitution(id)
        photoRepository.deleteByInstitution(id)
        InstitutionsTable.deleteWhere { InstitutionsTable.id eq id } > 0
    }

    private fun Institution.enrich(
        stats: InstitutionRatingStats?,
        photos: List<InstitutionPhoto>,
    ): Institution = copy(
        averageRating = stats?.averageRating,
        reviewCount = stats?.reviewCount ?: 0,
        photos = photos,
    )
}
