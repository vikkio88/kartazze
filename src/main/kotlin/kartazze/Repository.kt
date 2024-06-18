package org.vikkio.kartazze

import org.vikkio.kartazze.annotations.Id
import org.vikkio.kartazze.annotations.Table
import java.io.InvalidClassException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

typealias ColumnMap = Map<String, (Int, PreparedStatement) -> Unit>


abstract class Repository<EntityType : Any, IdType>(private val connection: Connection, private val entityClass: KClass<EntityType>) {
    private val table: String by lazy {
        entityClass.findAnnotation<Table>()?.name ?: entityClass.simpleName?.lowercase()
        ?: throw InvalidClassException("Class ${entityClass.simpleName} does not have correct Table configuration")
    }

    private val primaryKey: String by lazy {
        val keyProperty = entityClass.memberProperties.find { it.findAnnotation<Id>() != null }
        keyProperty?.name ?: "id"
    }

    private fun selectQ(columns: String = "*"): String {
        return "select $columns from $table"
    }

    private fun insertQ(columns: Iterable<String>): String {
        return "insert into $table (${columns.joinToString(",") { it }}) values (${columns.joinToString(",") { "?" }})"
    }

    private fun updateQ(columns: Iterable<String>): String {
        return "update $table set ${columns.joinToString(",") { "$it = ?" }}"
    }

    private fun deleteQ(): String {
        return "delete from $table "
    }


    fun findOne(id: IdType): EntityType? {
        val stm = connection.prepareStatement("${selectQ()} where $primaryKey = ?")
        when (id) {
            is String -> stm.setString(1, id)
            is Int -> stm.setInt(1, id)
            is Boolean -> stm.setBoolean(1, id)
            else -> throw Exception()
        }

        val result = stm.executeQuery()

        return if (result.next()) map(result) else null
    }

    fun all(): Iterable<EntityType> {
        val stm = connection.createStatement()
        val result = mutableListOf<EntityType>()

        val rs = stm.executeQuery(selectQ())
        while (rs.next()) {
            result.add(this.map(rs))
        }

        return result
    }

    fun filter(whereClause: String, vararg params: Any, pageSize: Int = 100, pageNumber: Int = 1): Iterable<EntityType> {
        val offset = (pageNumber - 1) * pageSize
        val sql = "${selectQ()} WHERE $whereClause LIMIT ? OFFSET ?"
        val stm = connection.prepareStatement(sql)
        params.forEachIndexed { index, param ->
            stm.setObject(index + 1, param)
        }

        // Set LIMIT and OFFSET parameters
        stm.setInt(params.size + 1, pageSize)
        stm.setInt(params.size + 2, offset)

        val result = mutableListOf<EntityType>()
        val rs = stm.executeQuery()
        while (rs.next()) {
            result.add(map(rs))
        }

        return result
    }

    fun create(entity: EntityType): Boolean {
        val mapped = map(entity)
        val stm = connection.prepareStatement(insertQ(mapped.keys))
        mapped.values.forEachIndexed { i, bind -> bind(i + 1, stm) }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun update(entity: EntityType, id: IdType): Boolean {
        val mapped = map(entity)
        val stm = connection.prepareStatement("${updateQ(mapped.keys)} where $primaryKey = '$id'")
        mapped.values.forEachIndexed { i, bind -> bind(i + 1, stm) }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun delete(id: IdType): Boolean {
        val stm = connection.prepareStatement("${deleteQ()} where $primaryKey = ?")
        when (id) {
            is String -> stm.setString(1, id)
            is Int -> stm.setInt(1, id)
            else -> throw Exception()
        }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun deleteWhere(condition: String, vararg params: Any): Boolean {
        val stm = connection.prepareStatement("${deleteQ()} where $condition")
        params.forEachIndexed { index, param ->
            stm.setObject(index + 1, param)
        }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    abstract fun map(rs: ResultSet): EntityType
    abstract fun map(obj: EntityType): ColumnMap
}