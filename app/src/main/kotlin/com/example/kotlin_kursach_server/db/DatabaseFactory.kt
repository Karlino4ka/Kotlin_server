package com.example.kotlin_kursach_server.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    private lateinit var dataSource: HikariDataSource

    fun init(jdbcUrl: String) {
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            driverClassName = driverClassNameFor(jdbcUrl)
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }

    fun createTables() {
        transaction {
            SchemaUtils.create(InstitutionsTable)
        }
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    private fun driverClassNameFor(jdbcUrl: String): String = when {
        jdbcUrl.startsWith("jdbc:h2:") -> "org.h2.Driver"
        jdbcUrl.startsWith("jdbc:postgresql:") -> "org.postgresql.Driver"
        else -> error("Неизвестный драйвер для URL: $jdbcUrl")
    }
}
