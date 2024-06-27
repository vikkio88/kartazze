package io.github.vikkio88.kartazze

import io.github.vikkio88.kartazze.annotations.Column
import io.github.vikkio88.kartazze.annotations.ColumnName
import io.github.vikkio88.kartazze.annotations.Id
import java.io.InvalidClassException
import java.sql.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


abstract class Repository<EntityType : Any, IdType>(
    private val connection: Connection,
    private val entityClass: KClass<EntityType>,
    private val dataMapper: IDataMapper<EntityType>? = null
) : IRepository<EntityType, IdType> {
    private var additionalJoins: String? = null
    private var additionalSelects: String? = null
    val stm: Statement = connection.createStatement()

    fun pstm(query: String): PreparedStatement {
        return connection.prepareStatement(query)
    }

    fun join(joins: String, selects: String): Repository<EntityType, IdType> {
        additionalJoins = joins
        additionalSelects = selects

        return this
    }

    fun with(relation: WithRelation): Repository<EntityType, IdType> {
        additionalJoins = relation.join
        additionalSelects = relation.select

        return this
    }

    private fun afterQuery() {
        additionalJoins = null
        additionalSelects = null
    }

    val table: String by lazy {
        SchemaHelper.getTableName(entityClass)
    }

    private val primaryKey: String by lazy {
        val keyProperty = entityClass.memberProperties.find { it.findAnnotation<Id>() != null }
        keyProperty?.let { property ->
            property.findAnnotation<Column>()?.name?.takeIf { it.isNotBlank() }
                ?: property.findAnnotation<ColumnName>()?.name?.takeIf { it.isNotBlank() }
                ?: property.name
        } ?: "id"
    }

    private fun selectQ(): String {
        var columns = "$table.*"
        if (dataMapper != null) {
            columns = dataMapper.selectColumns()
        }

        return "select $columns${if (additionalSelects != null) ", $additionalSelects" else ""} from $table"
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
        val stm =
            connection.prepareStatement("${selectQ()} ${if (additionalSelects != null) "$additionalJoins " else ""}where $table.$primaryKey = ?")
        when (id) {
            is String -> stm.setString(1, id)
            is Int -> stm.setInt(1, id)
            is Boolean -> stm.setBoolean(1, id)
            else -> throw Exception()
        }

        val result = stm.executeQuery()

        afterQuery()
        return if (result.next()) mapResultSetToEntity(result) else null
    }

    fun findOneWith(relations: HasManyRelation, id: IdType): EntityType? {
        val result = findOne(id) ?: return null


        for (p in relations.models) {
            val (linkedEntity, map) = p
            val linkedTable = SchemaHelper.getTableName(linkedEntity)
            val innerResults =
                stm.executeQuery("select ${map.mapper?.selectColumns() ?: "$linkedTable.*"} from $linkedTable inner join $table on $table.${map.localColumn} = $linkedTable.${map.externalColumn}")
            map.assignFunction(result, innerResults)
        }

        return result
    }

    override fun all(): Iterable<EntityType> {
        val result = mutableListOf<EntityType>()
        val rs = stm.executeQuery("${selectQ()} ${if (additionalSelects != null) "$additionalJoins " else ""}")
        afterQuery()

        while (rs.next()) {
            result.add(this.mapResultSetToEntity(rs))
        }

        return result
    }

    override fun filter(
        whereClause: String, vararg whereParams: Any, filters: FilterParams
    ): Iterable<EntityType> {
        var sql =
            "${selectQ()} ${if (additionalSelects != null) "$additionalJoins " else ""} WHERE $whereClause"
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
        afterQuery()
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
            val result = stm.executeUpdate() > 0
            result
        } catch (e: SQLException) {
            false
        } finally {
            afterQuery()
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
        } finally {
            afterQuery()
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
        } finally {
            afterQuery()
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
        } finally {
            afterQuery()
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
        } finally {
            afterQuery()
        }
    }

    override fun truncate(): Boolean {
        return try {
            connection.createStatement().executeQuery("delete from $table")
            true
        } catch (e: SQLException) {
            false
        } finally {
            afterQuery()
        }
    }

    override fun mapEntityToColumns(obj: EntityType): ColumnMap {
        return dataMapper?.mapEntityToColumns(obj)
            ?: throw InvalidClassException("You haven't specified a Data Mapper.")
    }

    override fun mapResultSetToEntity(rs: ResultSet): EntityType {
        return dataMapper?.mapResultSetToEntity(rs)
            ?: throw InvalidClassException("You haven't specified a Data Mapper.")
    }

}


