package com.example.kotlin_kursach_server

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class InstitutionRepositoryTest {

    private val repository = InstitutionRepository()

    @Test
    fun getAll_returnsNonEmptyList() {
        assert(repository.getAll().isNotEmpty())
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
