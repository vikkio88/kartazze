package io.github.vikkio88.kartazze


import io.github.vikkio88.kartazze.annotations.*
import io.github.vikkio88.kartazze.enums.DbType
import java.io.InvalidClassException
import java.security.InvalidParameterException
import java.sql.Connection
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

object SchemaHelper {
    fun crateTableIfNotExists(connection: Connection, entityClass: KClass<out Any>): Boolean {
        // Add different types of column mapping and triggers depending on the db type
        //val dbtype = getDatabaseType(connection)

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

                // Check for @ColumnType
                property.findAnnotation<ColumnType>()?.let {
                    typeToLookup = it.type
                }

                // Check for @Column
                property.findAnnotation<Column>()?.let {
                    typeToLookup = it.type
                    columnName = it.name
                }

                val columnType = when (typeToLookup) {
                    String::class -> "VARCHAR(255)"
                    Int::class -> "INTEGER"
                    Boolean::class -> "BOOLEAN"
                    else -> "TEXT"
                }

                // Check for @Id
                val idAnnotation = property.findAnnotation<Id>()
                val primaryKeyClause = if (idAnnotation != null) " PRIMARY KEY" else ""
                // Check for @AutoIncrement
                val autoIncrementAnnotation = property.findAnnotation<AutoIncrement>()
                val autoIncrementConstraint = if (columnType == "INTEGER" && autoIncrementAnnotation != null) {
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

        val timestampsColumns = if (entityClass.findAnnotation<Timestamps>() != null) {
            ", createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL"
        } else {
            ""
        }

        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS $tableName (
                $columns$timestampsColumns
            );
        """.trimIndent()

        val result = try {
            val statement = connection.createStatement()
            statement.execute(createTableSQL)
            true
        } catch (e: SQLException) {
            println("An error occurred while creating the table: ${e.message}")
            false
        }

        if (result && timestampsColumns.isNotBlank()) {
            val triggerSQL = """
                CREATE TRIGGER IF NOT EXISTS ${tableName}_updatedAt
                AFTER UPDATE ON $tableName
                FOR EACH ROW
                BEGIN
                    UPDATE $tableName SET updatedAt = CURRENT_TIMESTAMP WHERE rowid = NEW.rowid;
                END;
            """.trimIndent()

            try {
                val statement = connection.createStatement()
                statement.execute(triggerSQL)
            } catch (e: SQLException) {
                println("An error occurred while creating the trigger: ${e.message}")
            }
        }

        return result

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

    fun getDatabaseType(connection: Connection): DbType {
        val metaData = connection.metaData
        return DbType.fromProductName(metaData.databaseProductName)
            ?: throw InvalidParameterException("Db type '${metaData.databaseProductName}' not supported.")
    }
}