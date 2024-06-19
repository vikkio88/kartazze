package org.vikkio.org.vikkio.kartazze


import org.vikkio.org.vikkio.kartazze.annotations.Id
import org.vikkio.org.vikkio.kartazze.annotations.Ignore
import org.vikkio.org.vikkio.kartazze.annotations.Table
import org.vikkio.org.vikkio.kartazze.annotations.Unique
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
                val columnName = property.name
                val columnType = when (property.returnType.classifier) {
                    String::class -> "VARCHAR(255)"
                    Int::class -> "INT"
                    Boolean::class -> "BOOLEAN"
                    else -> "VARCHAR(255)" // default to VARCHAR for unsupported types
                }

                // Check for @Id
                val idAnnotation = property.findAnnotation<Id>()
                val primaryKeyClause = if (idAnnotation != null) " PRIMARY KEY" else ""

                // Check for @Unique
                val uniqueAnnotation = property.findAnnotation<Unique>()
                val uniqueConstraint = if (uniqueAnnotation != null) " UNIQUE" else ""


                "$columnName $columnType$primaryKeyClause$uniqueConstraint"
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