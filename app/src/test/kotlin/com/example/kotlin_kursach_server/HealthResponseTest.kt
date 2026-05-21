package com.example.kotlin_kursach_server

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthResponseTest {

    @Test
    fun healthResponse_hasOkStatus() {
        val response = HealthResponse(status = "ok")
        assertEquals("ok", response.status)
    }
}
