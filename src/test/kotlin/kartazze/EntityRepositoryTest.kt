package kartazze

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.BeforeEach
import org.vikkio.kartazze.ColumnMap
import org.vikkio.kartazze.DbHelper
import org.vikkio.kartazze.EntityRepository
import org.vikkio.kartazze.annotations.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

class UserEntity(connection: Connection) : EntityRepository<User, String>(connection, User::class) {
    override fun map(rs: ResultSet) = User(
        id = rs.getString("id"),
        name = rs.getString("name"),
        points = rs.getInt("points")
    )

    override fun map(obj: User): ColumnMap {
        return mapOf(
            "id" to { index, stm -> stm.setString(index, obj.id) },
            "name" to { index, stm -> stm.setString(index, obj.name) },
            "points" to { index, stm -> stm.setInt(index, obj.points) },
        )
    }
}

class EntityRepositoryTest {
    private val testConnection: Connection = DriverManager.getConnection("jdbc:sqlite::memory:")
    private val userE = UserEntity(testConnection)


    @BeforeEach
    fun setUp() {
        DbHelper.crateTableIfNotExists(testConnection, User::class)
    }

    @AfterEach
    fun tearDown() {
        DbHelper.dropTable(testConnection, User::class)
    }

    @Test
    fun findOne() {
        val found = userE.findOne("1")
        assertNull(found)

        val mario = User("1", "mario", 0, false)
        assertTrue(userE.create(mario))

        val mariodb = userE.findOne("1")
        assertEquals(mario, mariodb)
    }

    @Test
    fun withAnIgnoredParameterWillBeDifferentFromDb() {
        val generatedHere = User("1", "some.user", 0, true)
        assertTrue(userE.create(generatedHere))

        val fromDb = userE.findOne("1")!!
        assertNotEquals(generatedHere, fromDb)
    }

    @Test
    fun uniqueConstraintWillReturnFalseIfViolated() {
        val u = User("someId", "username", 0)
        val u2 = User("bonoVoxId", "username", 0)

        assertTrue(userE.create(u))
        assertFalse(userE.create(u2))
    }

    @Test
    fun selects() {
        // all
        assertEquals(0, userE.all().count())
        assertEquals(0, userE.filter("points = ?", 0).count())
        for (i in 1..10) {
            val mario = User("$i", "mario$i", i, false)
            assertTrue(userE.create(mario))
        }

        assertEquals(10, userE.all().count())
        assertEquals(1, userE.filter("points = ?", 1).count())
        assertEquals(2, userE.filter("points > ?", 0, pageSize = 2).count())
        assertEquals("mario1", userE.findOne("1")?.name)
    }


    @Test
    fun update() {
    }

    @Test
    fun deletes() {
    }

    @Test
    fun deleteWhere() {
    }

}