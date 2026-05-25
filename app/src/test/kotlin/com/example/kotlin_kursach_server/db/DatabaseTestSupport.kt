package com.example.kotlin_kursach_server.db

object DatabaseTestSupport {

    private const val H2_URL = "jdbc:h2:mem:institutions_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"

    fun setup() {
        DatabaseFactory.init(H2_URL)
        DatabaseFactory.createTables()
        InstitutionSeeder.seedIfEmpty()
    }

    fun tearDown() {
        DatabaseFactory.close()
    }
}
