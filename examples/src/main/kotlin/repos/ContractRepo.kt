package org.vikkio.repos

import io.github.vikkio88.kartazze.*
import org.vikkio.libs.JSON
import org.vikkio.models.Contract
import org.vikkio.models.Player
import org.vikkio.models.Team
import java.sql.Connection
import java.sql.ResultSet
import java.time.Month

class ContractRepo(connection: Connection) :
    Repository<Contract, String>(connection, Contract::class, ContractMapper()) {
    fun withTeamAndPlayer(): Repository<Contract, String> {
        return this.with(
            WithRelation(
                Contract::class,
                listOf(
                    Player::class to WithColMap("id", "playerId"),
                    Team::class to WithColMap("tId", "teamId")
                ),
                arrayOf("teams.*", "players.id as pId", "players.*")
            )
        )
    }
}

class ContractMapper : IDataMapper<Contract> {
    override fun mapResultSetToEntity(rs: ResultSet): Contract {
        return Contract(
            id = rs.getString("cId"),
            durationMonths = rs.getInt("durationMonths"),
            startYear = rs.getInt("startYear"),
            startMonth = Month.of(rs.getInt("startMonth")),
            wage = JSON.parse(rs.getString("wage")),
            player = if (rs.hasColumn("pId")) PlayerDataMapper().mapResultSetToEntity(rs) else null,
            team = if (rs.hasColumn("tId")) TeamMapper().mapResultSetToEntity(rs) else null,
        )
    }

    override fun mapEntityToColumns(obj: Contract) = columnMapOf(
        "cId" to { obj.id },
        "durationMonths" to { obj.durationMonths },
        "startYear" to { obj.startYear },
        "startMonth" to { obj.startMonth.value },
        "wage" to { JSON.stringify(obj.wage) },
        "playerId" to { obj.player?.id },
        "teamId" to { obj.team?.id }
    )
}