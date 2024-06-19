package org.vikkio.org.vikkio.kartazze

import org.vikkio.org.vikkio.kartazze.annotations.Id
import org.vikkio.org.vikkio.kartazze.annotations.Table
import java.io.InvalidClassException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


abstract class Repository<EntityType : Any, IdType>(
    private val connection: Connection,
    private val entityClass: KClass<EntityType>
) : IRepository<EntityType, IdType> {

    val stm: Statement = connection.createStatement()

    fun pstm(query: String): PreparedStatement {
        return connection.prepareStatement(query)
    }

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


    override fun findOne(id: IdType): EntityType? {
        val stm = connection.prepareStatement("${selectQ()} where $primaryKey = ?")
        when (id) {
            is String -> stm.setString(1, id)
            is Int -> stm.setInt(1, id)
            is Boolean -> stm.setBoolean(1, id)
            else -> throw Exception()
        }

        val result = stm.executeQuery()

        return if (result.next()) mapResultSetToEntity(result) else null
    }

    override fun all(): Iterable<EntityType> {
        val result = mutableListOf<EntityType>()
        val rs = stm.executeQuery(selectQ())
        while (rs.next()) {
            result.add(this.mapResultSetToEntity(rs))
        }

        return result
    }

    override fun filter(
        whereClause: String,
        vararg whereParams: Any,
        filters: FilterParams
    ): Iterable<EntityType> {
        var sql = "${selectQ()} WHERE $whereClause"
        if (filters.orderBy != null) {
            sql = "$sql ORDER BY ${filters.orderBy} ${if (filters.desc) "DESC" else "ASC"}"
        }
        sql = "$sql LIMIT ? OFFSET ?"
        val stm = pstm(sql)
        var index = 1
        whereParams.filter { it !is FilterParams }.forEach { param ->
            stm.setObject(index, param)
            index++
        }

        // Set LIMIT and OFFSET parameters
        stm.setInt(index, filters.limit)
        index++
        stm.setInt(index, filters.offset)

        val result = mutableListOf<EntityType>()
        val rs = stm.executeQuery()
        while (rs.next()) {
            result.add(mapResultSetToEntity(rs))
        }

        return result
    }

    override fun create(entity: EntityType): Boolean {
        val mapped = mapEntityToColumns(entity)
        val stm = pstm(insertQ(mapped.keys))
        mapped.values.forEachIndexed { i, bind -> bind(i + 1, stm) }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    override fun update(entity: EntityType, id: IdType): Boolean {
        val mapped = mapEntityToColumns(entity)
        val stm = pstm("${updateQ(mapped.keys)} where $primaryKey = '$id'")
        mapped.values.forEachIndexed { i, bind -> bind(i + 1, stm) }

        return try {
            stm.executeUpdate() > 0
        } catch (e: SQLException) {
            false
        }
    }

    override fun updateWhere(columns: ColumnMap, condition: String, vararg whereParams: Any): Int {
        val stm = pstm("${updateQ(columns.keys)} where $condition")
        var paramIndex = 1
        columns.values.forEach { bind ->
            bind(paramIndex, stm)
            paramIndex++
        }

        whereParams.forEach {
            stm.setObject(paramIndex, it)
            paramIndex++
        }

        return try {
            stm.executeUpdate()
        } catch (e: SQLException) {
            -1
        }
    }

    override fun delete(id: IdType): Boolean {
        val stm = pstm("${deleteQ()} where $primaryKey = ?")
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

    override fun deleteWhere(condition: String, vararg whereParams: Any): Int {
        val stm = connection.prepareStatement("${deleteQ()} where $condition")
        whereParams.forEachIndexed { index, param ->
            stm.setObject(index + 1, param)
        }

        return try {
            stm.executeUpdate()
        } catch (e: SQLException) {
            -1
        }
    }

    override fun truncate(): Boolean {
        return try {
            connection.createStatement().executeQuery("truncate table $table")
            true
        } catch (e: SQLException) {
            false
        }
    }

}

