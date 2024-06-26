package io.github.vikkio88.kartazze.tests

import io.github.vikkio88.kartazze.SchemaHelper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.sql.Connection
import java.sql.DriverManager


const val testFileName = "helpertest.db"

class SchemaHelperTest {

    @Test
    fun crateAndDropTables() {
        DriverManager.getConnection("jdbc:sqlite:$testFileName").use { connection ->
            assertFalse(tableExists(connection, "user2"))
        }
        DriverManager.getConnection("jdbc:sqlite:$testFileName").use { connection ->
            assertTrue(SchemaHelper.crateTableIfNotExists(connection, User2::class))
            assertTrue(tableExists(connection, "user2"))
        }
        DriverManager.getConnection("jdbc:sqlite:$testFileName").use { connection ->
            assertTrue(SchemaHelper.dropTable(connection, User2::class))
            assertFalse(tableExists(connection, "user2"))
        }
    }

    private fun tableExists(connection: Connection, tableName: String): Boolean {
        // postgres
        // val query = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = ?)"
//        preparedStatement.setString(1, tableName)
//        return resultSet.next() && resultSet.getBoolean(1)
        val query = "PRAGMA table_info($tableName)"
        val preparedStatement = connection.prepareStatement(query)
        val resultSet = preparedStatement.executeQuery()
//        preparedStatement.close()
        return resultSet.next()
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDown() {
            val file = File(testFileName)
            if (file.exists()) {
                file.delete()
            }
        }
    }
}