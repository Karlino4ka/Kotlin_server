package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.DatabaseTestSupport
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InstitutionRepositoryTest {

    private val reviewRepository = ReviewRepository()
    private val photoRepository = InstitutionPhotoRepository()
    private val repository = InstitutionRepository(reviewRepository, photoRepository)

    @Before
    fun setUp() {
        DatabaseTestSupport.setup()
    }

    @After
    fun tearDown() {
        DatabaseTestSupport.tearDown()
    }

    @Test
    fun getAll_returnsSeededList() {
        val institutions = repository.getAll()
        assertTrue(institutions.isNotEmpty())
    }

    @Test
    fun getById_existingId_returnsInstitution() {
        val institution = repository.getById("1")
        assertNotNull(institution)
        assertEquals("1", institution?.id)
    }

    @Test
    fun getById_unknownId_returnsNull() {
        assertNull(repository.getById("unknown"))
    }

    @Test
    fun update_existingId_changesFields() {
        val original = repository.getById("1")!!
        val updated = repository.update(
            "1",
            CreateInstitutionRequest(
                name = "Обновлённое название",
                type = original.type,
                city = original.city,
                address = original.address,
                description = original.description,
                phone = original.phone,
                website = original.website,
            ),
        )
        assertNotNull(updated)
        assertEquals("Обновлённое название", updated?.name)
    }

    @Test
    fun delete_existingId_removesInstitution() {
        val created = repository.create(
            CreateInstitutionRequest(
                name = "Временное",
                type = InstitutionType.SCHOOL,
                city = "Минск",
                address = "ул. Тест",
                description = "Тест",
            ),
        )
        assertTrue(repository.delete(created.id))
        assertNull(repository.getById(created.id))
    }
}
