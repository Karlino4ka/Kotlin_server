package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.DatabaseTestSupport
import com.example.kotlin_kursach_server.storage.PhotoStorage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InstitutionPhotoRepositoryTest {

    private val repository = InstitutionPhotoRepository()

    @Before
    fun setUp() {
        DatabaseTestSupport.setup()
    }

    @After
    fun tearDown() {
        DatabaseTestSupport.tearDown()
    }

    @Test
    fun addUploadedFile_andDelete() {
        val photo = repository.addUploadedFile(
            institutionId = "1",
            bytes = byteArrayOf(-1, -40, -1, -32),
            extension = "jpg",
        )

        assertTrue(photo.url.contains("/uploads/1/"))
        val photos = repository.getByInstitution("1")
        assertTrue(photos.any { it.id == photo.id })

        assertTrue(repository.delete("1", photo.id))
        assertFalse(repository.getByInstitution("1").any { it.id == photo.id })
    }

    @Test
    fun addUrl_externalLink_storesAsIs() {
        val externalUrl = "https://example.com/photo.jpg"
        val photo = repository.addUrl("1", externalUrl)

        assertEquals(externalUrl, photo.url)
    }

    @Test
    fun institutionIncludesPhotos_afterAdd() {
        repository.addUrl("2", "https://example.com/campus.png")

        val institutionRepository = InstitutionRepository(ReviewRepository(), repository)
        val institution = institutionRepository.getById("2")

        assertNotNull(institution)
        assertEquals(1, institution!!.photos.size)
    }
}

class PhotoStorageTest {

    @Test
    fun toRelativeUploadPath_extractsUploadsPath() {
        val path = PhotoStorage.toRelativeUploadPath("http://10.0.2.2:8080/uploads/abc/photo.jpg")
        assertEquals("/uploads/abc/photo.jpg", path)
    }

    @Test
    fun toRelativeUploadPath_keepsRelativePath() {
        assertEquals("/uploads/x/y.png", PhotoStorage.toRelativeUploadPath("/uploads/x/y.png"))
    }
}
