package org.vikkio.repos

import io.github.vikkio88.kartazze.Repository
import io.github.vikkio88.kartazze.columnMapOf
import org.vikkio.models.LogEntry
import java.sql.Connection
import java.sql.ResultSet

class LogRepo(connection: Connection) : Repository<LogEntry, Int>(connection, LogEntry::class) {
    override fun mapResultSetToEntity(rs: ResultSet) = LogEntry(
        id = rs.getInt("id"),
        entry = rs.getString("entry"),
        createdAt = rs.getTimestamp("createdAt"),
        updatedAt = rs.getTimestamp("updatedAt"),
    )

    override fun mapEntityToColumns(obj: LogEntry) =
        columnMapOf("entry" to { obj.entry })

}