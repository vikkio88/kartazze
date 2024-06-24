package org.vikkio.repos

import io.github.vikkio88.kartazze.IDataMapper
import io.github.vikkio88.kartazze.Repository
import io.github.vikkio88.kartazze.columnMapOf
import org.vikkio.libs.JSON
import org.vikkio.models.Contract
import java.sql.Connection
import java.sql.ResultSet
import java.time.Month

class ContractRepo(connection: Connection) :
    Repository<Contract, String>(connection, Contract::class, ContractMapper())

class ContractMapper : IDataMapper<Contract> {

    override fun selectColumns(): String {
        return "contracts.id as cId, contracts.*"
    }

    override fun mapResultSetToEntity(rs: ResultSet): Contract {

        return Contract(
            id = rs.getString("cId"),
            durationMonths = rs.getInt("durationMonths"),
            startYear = rs.getInt("startYear"),
            startMonth = Month.of(rs.getInt("startMonth")),
            wage = JSON.parse(rs.getString("wage")),
            player = if (rs.getString("pId") != null) PlayerDataMapper().mapResultSetToEntity(rs) else null,
            team = if (rs.getString("tId") != null) TeamMapper().mapResultSetToEntity(rs) else null,
        )
    }

    override fun mapEntityToColumns(obj: Contract) = columnMapOf(
        "id" to { obj.id },
        "durationMonths" to { obj.durationMonths },
        "startYear" to { obj.startYear },
        "startMonth" to { obj.startMonth.value },
        "wage" to { JSON.stringify(obj.wage) },
        "playerId" to { obj.player?.id },
        "teamId" to { obj.team?.id }
    )
}