package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.DatabaseTestSupport
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReviewRepositoryTest {

    private val repository = ReviewRepository()

    @Before
    fun setUp() {
        DatabaseTestSupport.setup()
    }

    @After
    fun tearDown() {
        DatabaseTestSupport.tearDown()
    }

    @Test
    fun createOrUpdate_addsReviewAndCalculatesAverage() {
        repository.createOrUpdate(
            institutionId = "1",
            userId = "user-1",
            userEmail = "user@test.com",
            authorName = "Тест",
            request = CreateReviewRequest(rating = 5, text = "Отличное место"),
        )

        val reviews = repository.getByInstitution("1")
        assertEquals(1, reviews.size)
        assertEquals(5, reviews.first().rating)

        val stats = repository.statsForInstitution("1")
        assertEquals(1, stats.reviewCount)
        assertEquals(5.0, stats.averageRating!!, 0.01)
    }

    @Test
    fun createOrUpdate_sameUser_updatesExistingReview() {
        repository.createOrUpdate(
            institutionId = "1",
            userId = "user-2",
            userEmail = "user2@test.com",
            authorName = null,
            request = CreateReviewRequest(rating = 3, text = "Нормально"),
        )
        repository.createOrUpdate(
            institutionId = "1",
            userId = "user-2",
            userEmail = "user2@test.com",
            authorName = null,
            request = CreateReviewRequest(rating = 5, text = "Стало лучше"),
        )

        val reviews = repository.getByInstitution("1").filter { it.userId == "user-2" }
        assertEquals(1, reviews.size)
        assertEquals(5, reviews.first().rating)
        assertEquals("Стало лучше", reviews.first().text)
    }

    @Test
    fun institutionWithReviews_includesRatingInGetById() {
        repository.createOrUpdate(
            institutionId = "2",
            userId = "user-3",
            userEmail = "r@test.com",
            authorName = null,
            request = CreateReviewRequest(rating = 4, text = "Хорошо"),
        )

        val photoRepository = InstitutionPhotoRepository()
        val institutionRepository = InstitutionRepository(repository, photoRepository)
        val institution = institutionRepository.getById("2")

        assertNotNull(institution)
        assertEquals(1, institution!!.reviewCount)
        assertNotNull(institution.averageRating)
    }
}
