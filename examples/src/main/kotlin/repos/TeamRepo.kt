package org.vikkio.repos

import io.github.vikkio88.kartazze.*
import org.vikkio.models.Contract
import org.vikkio.models.Team
import java.sql.Connection
import java.sql.ResultSet

val contractMapper = ContractMapper()

class TeamRepo(connection: Connection) : Repository<Team, String>(connection, Team::class, TeamMapper()) {
    fun oneWithContracts(id: String): Team? {
        return findOneWith(
            HasManyRelation(
                listOf(
                    Contract::class to
                            HasColMap("teamId", "tId", fun(main: Any, children: ResultSet) {
                                if (main !is Team) return
                                val result = mutableListOf<Contract>()
                                while (children.next()) {
                                    result.add(contractMapper.mapResultSetToEntity(children))
                                }

                                main.contracts = result
                            })
                )
            ), id
        )

    }

}

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