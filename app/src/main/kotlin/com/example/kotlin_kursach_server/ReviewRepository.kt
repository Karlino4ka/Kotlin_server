package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.ReviewsTable
import com.example.kotlin_kursach_server.db.toReview
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.avg
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

private val createdAtFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

class ReviewRepository {

    fun getByInstitution(institutionId: String): List<Review> = transaction {
        ReviewsTable
            .selectAll()
            .where { ReviewsTable.institutionId eq institutionId }
            .orderBy(ReviewsTable.createdAt to SortOrder.DESC)
            .map { it.toReview() }
    }

    fun ratingStatsByInstitution(): Map<String, InstitutionRatingStats> = transaction {
        val rows = ReviewsTable
            .select(ReviewsTable.institutionId, ReviewsTable.rating.avg(), ReviewsTable.id.count())
            .groupBy(ReviewsTable.institutionId)

        rows.associate { row ->
            val institutionId = row[ReviewsTable.institutionId]
            val count = row[ReviewsTable.id.count()].toInt()
            val avg = row[ReviewsTable.rating.avg()]?.toDouble()
            institutionId to InstitutionRatingStats(
                averageRating = avg?.let { roundRating(it) },
                reviewCount = count,
            )
        }
    }

    fun statsForInstitution(institutionId: String): InstitutionRatingStats = transaction {
        val rows = ReviewsTable
            .select(ReviewsTable.rating.avg(), ReviewsTable.id.count())
            .where { ReviewsTable.institutionId eq institutionId }
            .groupBy(ReviewsTable.institutionId)

        val row = rows.firstOrNull() ?: return@transaction InstitutionRatingStats()
        val count = row[ReviewsTable.id.count()].toInt()
        val avg = row[ReviewsTable.rating.avg()]?.toDouble()
        InstitutionRatingStats(
            averageRating = avg?.let { roundRating(it) },
            reviewCount = count,
        )
    }

    private fun roundRating(value: Double): Double =
        kotlin.math.round(value * 10) / 10.0

    private fun nowCreatedAt(): String = LocalDateTime.now().format(createdAtFormatter)

    fun createOrUpdate(
        institutionId: String,
        userId: String,
        userEmail: String,
        authorName: String?,
        request: CreateReviewRequest,
    ): Review = transaction {
        val existing = ReviewsTable
            .selectAll()
            .where { (ReviewsTable.institutionId eq institutionId) and (ReviewsTable.userId eq userId) }
            .firstOrNull()

        if (existing != null) {
            val reviewId = existing[ReviewsTable.id]
            ReviewsTable.update({ ReviewsTable.id eq reviewId }) {
                it[ReviewsTable.rating] = request.rating
                it[ReviewsTable.text] = request.text.trim()
                it[ReviewsTable.userEmail] = userEmail
                it[ReviewsTable.authorName] = authorName?.trim()?.takeIf { name -> name.isNotEmpty() }
                it[ReviewsTable.createdAt] = nowCreatedAt()
            }
            getById(reviewId) ?: error("Failed to read updated review")
        } else {
            val id = UUID.randomUUID().toString()
            ReviewsTable.insert {
                it[ReviewsTable.id] = id
                it[ReviewsTable.institutionId] = institutionId
                it[ReviewsTable.userId] = userId
                it[ReviewsTable.userEmail] = userEmail
                it[ReviewsTable.authorName] = authorName?.trim()?.takeIf { name -> name.isNotEmpty() }
                it[ReviewsTable.rating] = request.rating
                it[ReviewsTable.text] = request.text.trim()
                it[ReviewsTable.createdAt] = nowCreatedAt()
            }
            getById(id) ?: error("Failed to read created review")
        }
    }

    fun deleteByInstitution(institutionId: String) {
        ReviewsTable.deleteWhere { ReviewsTable.institutionId eq institutionId }
    }

    private fun getById(id: String): Review? = transaction {
        ReviewsTable
            .selectAll()
            .where { ReviewsTable.id eq id }
            .map { it.toReview() }
            .singleOrNull()
    }
}
