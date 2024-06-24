package io.github.vikkio88.kartazze

import java.sql.ResultSet

interface IDataMapper<T> {
    fun selectColumns() = "*"
    fun mapResultSetToEntity(rs: ResultSet): T
    fun mapEntityToColumns(obj: T): ColumnMap
}
