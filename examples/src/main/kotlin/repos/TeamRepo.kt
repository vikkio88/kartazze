package org.vikkio.repos

import io.github.vikkio88.kartazze.IDataMapper
import io.github.vikkio88.kartazze.Repository
import io.github.vikkio88.kartazze.columnMapOf
import org.vikkio.models.Team
import java.sql.Connection
import java.sql.ResultSet

class TeamRepo(connection: Connection) : Repository<Team, String>(connection, Team::class, TeamMapper())

class TeamMapper : IDataMapper<Team> {
    override fun mapResultSetToEntity(rs: ResultSet): Team {
        return Team(
            name = rs.getString("tName"),
            id = rs.getString("tId")
        )
    }

    override fun mapEntityToColumns(obj: Team) = columnMapOf(
        "tId" to { obj.id },
        "tName" to { obj.name }
    )

}