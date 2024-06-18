package org.vikkio.kartazze

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

typealias ColumnMap = Map<String, (Int, PreparedStatement) -> Unit>


abstract class Entity<T, Id>(private val connection: Connection) {
    abstract val table: String
    abstract val primaryKey: String

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


    fun findOne(id: Id): T? {
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

    fun all(): Iterable<T> {
        val stm = connection.createStatement()
        val result = mutableListOf<T>()

        val rs = stm.executeQuery(selectQ())
        while (rs.next()) {
            result.add(this.map(rs))
        }

        return result
    }

    fun filter(whereClause: String, vararg params: Any, pageSize: Int = 100, pageNumber: Int = 1): Iterable<T> {
        val offset = (pageNumber - 1) * pageSize
        val sql = "${selectQ()} WHERE $whereClause LIMIT ? OFFSET ?"
        val stm = connection.prepareStatement(sql)
        params.forEachIndexed { index, param ->
            stm.setObject(index + 1, param)
        }

        // Set LIMIT and OFFSET parameters
        stm.setInt(params.size + 1, pageSize)
        stm.setInt(params.size + 2, offset)

        val result = mutableListOf<T>()
        val rs = stm.executeQuery()
        while (rs.next()) {
            result.add(map(rs))
        }

        return result
    }

    fun create(entity: T): Boolean {
        val mapped = map(entity)
        val stm = connection.prepareStatement(insertQ(mapped.keys))
        mapped.values.forEachIndexed { i, bind -> bind(i + 1, stm) }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun update(entity: T, id: Id): Boolean {
        val mapped = map(entity)
        val stm = connection.prepareStatement("${updateQ(mapped.keys)} where $primaryKey = '$id'")
        mapped.values.forEachIndexed { i, bind -> bind(i + 1, stm) }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    fun delete(id: Id): Boolean {
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

    abstract fun map(rs: ResultSet): T
    abstract fun map(obj: T): ColumnMap
}