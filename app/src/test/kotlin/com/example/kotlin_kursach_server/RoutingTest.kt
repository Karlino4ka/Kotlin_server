package com.example.kotlin_kursach_server

import com.example.kotlin_kursach_server.db.DatabaseTestSupport
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RoutingTest {

    @Before
    fun setUp() {
        DatabaseTestSupport.setup()
    }

    @After
    fun tearDown() {
        DatabaseTestSupport.tearDown()
    }

    @Test
    fun health_returnsOk() = testApplication {
        configureTestApp()
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("ok"))
    }

    @Test
    fun getInstitutions_returnsList() = testApplication {
        configureTestApp()
        val response = client.get("/institutions")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("name"))
    }

    @Test
    fun postInstitution_withoutAdmin_returnsForbidden() = testApplication {
        configureTestApp()
        val response = client.post("/institutions") {
            contentType(ContentType.Application.Json)
            setBody(
                """{"name":"Новое","type":"SCHOOL","city":"Минск","address":"ул. 1","description":"Описание"}""",
            )
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun postInstitution_withAdminEmail_createsInstitution() = testApplication {
        configureTestApp()
        val response = client.post("/institutions") {
            contentType(ContentType.Application.Json)
            header("X-User-Email", "admin@test.com")
            setBody(
                """{"name":"Тестовая школа","type":"SCHOOL","city":"Минск","address":"ул. 1","description":"Описание"}""",
            )
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("Тестовая школа"))
    }

    @Test
    fun postReview_withoutAuth_returnsUnauthorized() = testApplication {
        configureTestApp()
        val response = client.post("/institutions/1/reviews") {
            contentType(ContentType.Application.Json)
            setBody("""{"rating":5,"text":"Отзыв"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun postReview_withUserHeaders_createsReview() = testApplication {
        configureTestApp()
        val response = client.post("/institutions/1/reviews") {
            contentType(ContentType.Application.Json)
            header("X-User-Id", "user-100")
            header("X-User-Email", "student@test.com")
            setBody("""{"rating":4,"text":"Хороший вуз"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    private fun ApplicationTestBuilder.configureTestApp() {
        val reviewRepository = ReviewRepository()
        val photoRepository = InstitutionPhotoRepository()
        val institutionRepository = InstitutionRepository(reviewRepository, photoRepository)
        application {
            install(ContentNegotiation) { json() }
            configureRouting(institutionRepository, reviewRepository, photoRepository)
        }
    }
}
