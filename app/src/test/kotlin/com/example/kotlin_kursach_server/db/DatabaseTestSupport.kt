package com.example.kotlin_kursach_server.db

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseTestSupport {

    private var databaseName = "institutions_test_${System.nanoTime()}"

    fun setup() {
        databaseName = "institutions_test_${System.nanoTime()}"
        val jdbcUrl = "jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
        DatabaseFactory.init(jdbcUrl)
        transaction {
            SchemaUtils.drop(InstitutionPhotosTable, ReviewsTable, InstitutionsTable)
        }
        DatabaseFactory.createTables()
        InstitutionSeeder.seedIfEmpty()
    }

    fun tearDown() {
        DatabaseFactory.close()
    }
}
