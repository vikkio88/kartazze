package io.github.vikkio88.kartazze


import io.github.vikkio88.kartazze.annotations.*
import java.io.InvalidClassException
import java.sql.Connection
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

object SchemeHelper {
    fun crateTableIfNotExists(connection: Connection, entityClass: KClass<out Any>): Boolean {
        val tableName = getTableName(entityClass)
        val columns =
            entityClass.memberProperties.filter { it.findAnnotation<Ignore>() == null }.joinToString(", ") { property ->
                var columnName = property.name
                var typeToLookup = property.returnType.classifier
                // Check for @References
                val referencesAnnotation = property.findAnnotation<References>()
                val referencesConstraint = referencesAnnotation?.let {
                    typeToLookup = it.externalIdType
                    columnName = it.columnName
                    " REFERENCES ${it.externalTable}(${it.externalColumn})"
                } ?: ""

                val columnType = when (typeToLookup) {
                    String::class -> "VARCHAR(255)"
                    Int::class -> "INT"
                    Boolean::class -> "BOOLEAN"
                    else -> "VARCHAR(255)"
                }

                // Check for @Id
                val idAnnotation = property.findAnnotation<Id>()
                val primaryKeyClause = if (idAnnotation != null) " PRIMARY KEY" else ""
                // Check for @AutoIncrement
                val autoIncrementAnnotation = property.findAnnotation<AutoIncrement>()
                val autoIncrementConstraint = if (columnType == "INT" && autoIncrementAnnotation != null) {
                    " AUTOINCREMENT"
                } else {
                    ""
                }

                // Check for @Unique
                val uniqueAnnotation = property.findAnnotation<Unique>()
                val uniqueConstraint = if (uniqueAnnotation != null) " UNIQUE" else ""

                // Check if nullable
                val nullableConstraint = if (property.returnType.isMarkedNullable) "" else " NOT NULL"

                "$columnName $columnType$primaryKeyClause$uniqueConstraint$autoIncrementConstraint$referencesConstraint$nullableConstraint"
            }

        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS $tableName (
                $columns
            );
        """.trimIndent()

        return try {
            val statement = connection.createStatement()
            statement.execute(createTableSQL)
            true
        } catch (e: SQLException) {
            println("An error occurred while creating the table: ${e.message}")
            false
        }
    }

    fun dropTable(connection: Connection, entityClass: KClass<out Any>): Boolean {
        val tableName = getTableName(entityClass)
        return try {
            val stm = connection.createStatement()
            stm.execute("DROP TABLE $tableName")
            true
        } catch (e: SQLException) {
            println("An error occurred while dropping the table: ${e.message}")
            false
        }
    }

    private fun getTableName(entityClass: KClass<out Any>): String {
        val tableName = entityClass.findAnnotation<Table>()?.name ?: entityClass.simpleName?.lowercase()
        ?: throw InvalidClassException("Class ${entityClass.simpleName} does not have correct Table configuration")
        return tableName
    }
}