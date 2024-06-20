package org.vikkio.repos

import io.github.vikkio88.kartazze.ColumnMap
import io.github.vikkio88.kartazze.Repository
import io.github.vikkio88.kartazze.columnMapOf
import org.vikkio.models.Player
import org.vikkio.models.Role
import java.sql.Connection
import java.sql.ResultSet

class PlayerRepo(connection: Connection) : Repository<Player, String>(connection, Player::class) {
    override fun mapResultSetToEntity(rs: ResultSet) = Player(
        id = rs.getString("id"),
        name = rs.getString("name"),
        surname = rs.getString("surname"),
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
        )
    }
}