package io.github.vikkio88.kartazze.tests

import io.github.vikkio88.kartazze.*
import io.github.vikkio88.kartazze.annotations.*
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

class UserRepository(connection: Connection) : Repository<User, String>(connection, User::class, UserMapper())

class UserMapper : IDataMapper<User> {
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

class TeamRepository(connection: Connection) :
    Repository<TeamWithRel, Int>(connection, TeamWithRel::class, TeamMapper())

class TeamMapper : IDataMapper<TeamWithRel> {
    override fun mapResultSetToEntity(rs: ResultSet): TeamWithRel {
        return TeamWithRel(
            id = rs.getInt("tId"),
            name = rs.getString("tName"),
            captain = if (rs.hasColumn("id")) UserMapper().mapResultSetToEntity(rs) else null,
        )
    }

    override fun mapEntityToColumns(obj: TeamWithRel): ColumnMap {
        TODO("Not yet implemented")
    }

}

data class TeamWithRel(
    @Id
    @ColumnName("tId")
    val id: Int,
    @ColumnName("tName")
    val name: String,
    @References(
        externalTable = "users",
        externalColumn = "id",
        externalIdType = String::class,
        columnName = "captainId"
    )
    val captain: User? = null,
    @Ignore
    val roster: List<User>? = null
)


data class User2(
    @Id
    val id: Int,
    @Ignore
    val ciao: String
)