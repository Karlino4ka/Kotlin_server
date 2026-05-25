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

    private val repository = InstitutionRepository()

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
}
