package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.InstitutionPhotosTable
import com.example.kotlin_kursach_server.db.toInstitutionPhoto
import com.example.kotlin_kursach_server.storage.PhotoStorage
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class InstitutionPhotoRepository {

    fun getByInstitution(institutionId: String): List<InstitutionPhoto> = transaction {
        InstitutionPhotosTable
            .selectAll()
            .where { InstitutionPhotosTable.institutionId eq institutionId }
            .orderBy(InstitutionPhotosTable.sortOrder to SortOrder.ASC)
            .map { it.toInstitutionPhoto() }
    }

    fun photosByInstitution(): Map<String, List<InstitutionPhoto>> = transaction {
        InstitutionPhotosTable
            .selectAll()
            .orderBy(
                InstitutionPhotosTable.institutionId to SortOrder.ASC,
                InstitutionPhotosTable.sortOrder to SortOrder.ASC,
            )
            .groupBy(
                keySelector = { it[InstitutionPhotosTable.institutionId] },
                valueTransform = { it.toInstitutionPhoto() },
            )
    }

    fun addUrl(institutionId: String, url: String): InstitutionPhoto = transaction {
        val id = UUID.randomUUID().toString()
        val sortOrder = nextSortOrder(institutionId)
        val normalizedUrl = url.trim()
        val storedUrl = PhotoStorage.toRelativeUploadPath(normalizedUrl) ?: normalizedUrl
        InstitutionPhotosTable.insert {
            it[InstitutionPhotosTable.id] = id
            it[InstitutionPhotosTable.institutionId] = institutionId
            it[InstitutionPhotosTable.url] = storedUrl
            it[InstitutionPhotosTable.sortOrder] = sortOrder
        }
        getById(id) ?: InstitutionPhoto(id = id, url = normalizedUrl)
    }

    fun addUploadedFile(institutionId: String, bytes: ByteArray, extension: String): InstitutionPhoto =
        transaction {
            val url = PhotoStorage.saveUploadedFile(institutionId, bytes, extension)
            val id = UUID.randomUUID().toString()
            val sortOrder = nextSortOrder(institutionId)
            InstitutionPhotosTable.insert {
                it[InstitutionPhotosTable.id] = id
                it[InstitutionPhotosTable.institutionId] = institutionId
                it[InstitutionPhotosTable.url] = url
                it[InstitutionPhotosTable.sortOrder] = sortOrder
            }
            getById(id) ?: InstitutionPhoto(id = id, url = url)
        }

    fun delete(institutionId: String, photoId: String): Boolean = transaction {
        val photo = InstitutionPhotosTable
            .selectAll()
            .where {
                (InstitutionPhotosTable.id eq photoId) and
                    (InstitutionPhotosTable.institutionId eq institutionId)
            }
            .firstOrNull()
            ?: return@transaction false

        val url = photo[InstitutionPhotosTable.url]
        PhotoStorage.deleteByUrl(url)
        InstitutionPhotosTable.deleteWhere { InstitutionPhotosTable.id eq photoId } > 0
    }

    fun deleteByInstitution(institutionId: String) {
        transaction {
            val urls = InstitutionPhotosTable
                .selectAll()
                .where { InstitutionPhotosTable.institutionId eq institutionId }
                .map { it[InstitutionPhotosTable.url] }
            urls.forEach { PhotoStorage.deleteByUrl(it) }
            InstitutionPhotosTable.deleteWhere { InstitutionPhotosTable.institutionId eq institutionId }
        }
    }

    private fun nextSortOrder(institutionId: String): Int =
        InstitutionPhotosTable
            .selectAll()
            .where { InstitutionPhotosTable.institutionId eq institutionId }
            .count()
            .toInt()

    private fun getById(id: String): InstitutionPhoto? =
        InstitutionPhotosTable
            .selectAll()
            .where { InstitutionPhotosTable.id eq id }
            .map { it.toInstitutionPhoto() }
            .singleOrNull()
}
