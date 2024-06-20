package io.github.vikkio88.kartazze

import java.sql.ResultSet

interface IRepository<EntityType : Any, IdType> {

    fun findOne(id: IdType): EntityType?
    fun all(): Iterable<EntityType>
    fun filter(
        whereClause: String,
        vararg whereParams: Any,
        filters: FilterParams = FilterParams()
    ): Iterable<EntityType>

    fun create(entity: EntityType): Boolean
    fun update(entity: EntityType, id: IdType): Boolean
    fun updateWhere(columns: ColumnMap, condition: String, vararg whereParams: Any): Int
    fun delete(id: IdType): Boolean
    fun deleteWhere(condition: String, vararg whereParams: Any): Int
    fun truncate(): Boolean

    fun mapResultSetToEntity(rs: ResultSet): EntityType
    fun mapEntityToColumns(obj: EntityType): ColumnMap
}