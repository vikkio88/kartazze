package io.github.vikkio88.kartazze

import java.sql.ResultSet
import java.sql.SQLException

interface IDataMapper<T> {
    fun selectColumns() = "*"
    fun mapResultSetToEntity(rs: ResultSet): T
    fun mapEntityToColumns(obj: T): ColumnMap
}


fun ResultSet.hasColumn(columnName: String): Boolean {
    return try {
        this.findColumn(columnName)
        true
    } catch (e: SQLException) {
        false
    }
}