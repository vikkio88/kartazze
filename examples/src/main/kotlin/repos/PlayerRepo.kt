package org.vikkio.repos

import io.github.vikkio88.kartazze.*
import org.vikkio.models.Player
import org.vikkio.models.Role
import java.sql.Connection
import java.sql.ResultSet

class PlayerRepo(connection: Connection) :
    Repository<Player, String>(connection, Player::class, dataMapper = PlayerDataMapper())

class PlayerDataMapper : IDataMapper<Player> {
    override fun selectColumns() = "players.id as pId, players.*"

    override fun mapResultSetToEntity(rs: ResultSet) = Player(
        id = rs.getString("pId"),
        name = rs.getString("name"),
        surname = rs.getString("surname"),
        nickname = rs.getString("nickname"),
        role = Role.valueOf(rs.getString("role")),
        skill = rs.getInt("skill"),
        age = rs.getInt("age")
    )

    override fun mapEntityToColumns(obj: Player): ColumnMap {
        return columnMapOf(
            "id" to { obj.id },
            "name" to { obj.name },
            "surname" to { obj.surname },
            "role" to { obj.role.name },
            "age" to { obj.age },
            "skill" to { obj.skill },
            "nickname" to { obj.nickname }
        )
    }
}