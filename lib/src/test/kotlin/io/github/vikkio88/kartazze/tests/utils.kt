package io.github.vikkio88.kartazze.tests

import io.github.vikkio88.kartazze.ColumnMap
import io.github.vikkio88.kartazze.Repository
import io.github.vikkio88.kartazze.annotations.Id
import io.github.vikkio88.kartazze.annotations.Ignore
import io.github.vikkio88.kartazze.annotations.Table
import io.github.vikkio88.kartazze.annotations.Unique
import io.github.vikkio88.kartazze.columnMapOf
import java.sql.Connection
import java.sql.ResultSet


@Table(name = "users")
data class User(
    @Id
    val id: String,
    @Unique
    val name: String,
    val points: Int,
    @Ignore
    val stuff: Boolean = false
)

class UserRepository(connection: Connection) : Repository<User, String>(connection, User::class) {
    override fun mapResultSetToEntity(rs: ResultSet) = User(
        id = rs.getString("id"),
        name = rs.getString("name"),
        points = rs.getInt("points")
    )

    override fun mapEntityToColumns(obj: User): ColumnMap {
        return columnMapOf(
            "id" to { obj.id },
            "name" to { obj.name },
            "points" to { obj.points },
        )
    }
}

data class User2(
    @Id
    val id: Int,
    @Ignore
    val ciao: String
)
