package kartazze

import org.vikkio.org.vikkio.kartazze.ColumnMap
import org.vikkio.org.vikkio.kartazze.Repository
import org.vikkio.org.vikkio.kartazze.annotations.Id
import org.vikkio.org.vikkio.kartazze.annotations.Ignore
import org.vikkio.org.vikkio.kartazze.annotations.Table
import org.vikkio.org.vikkio.kartazze.annotations.Unique
import org.vikkio.org.vikkio.kartazze.columnMapOf
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
