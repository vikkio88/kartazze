package io.github.vikkio88.kartazze.tests

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.github.vikkio88.kartazze.SchemeHelper
import io.github.vikkio88.kartazze.filters
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertNull
import kotlin.test.assertTrue


class RepositoryTest {
    private val testConnection: Connection = DriverManager.getConnection("jdbc:sqlite::memory:")
    private val userRepository = UserRepository(testConnection)

    init {
        SchemeHelper.crateTableIfNotExists(testConnection, User::class)
    }


    @AfterEach
    fun tearDown() {
        userRepository.truncate()
    }

    @Test
    fun findOne() {
        val found = userRepository.findOne("1")
        assertNull(found)

        val mario = User("1", "mario", 0, false)
        assertTrue(userRepository.create(mario))

        val mariodb = userRepository.findOne("1")
        assertEquals(mario, mariodb)
    }

    @Test
    fun withAnIgnoredParameterWillBeDifferentFromDb() {
        val generatedHere = User("1", "some.user", 0, true)
        assertTrue(userRepository.create(generatedHere))

        val fromDb = userRepository.findOne("1")!!
        assertNotEquals(generatedHere, fromDb)
    }

    @Test
    fun uniqueConstraintWillReturnFalseIfViolated() {
        val u = User("someId", "username", 0)
        val u2 = User("bonoVoxId", "username", 0)

        assertTrue(userRepository.create(u))
        assertFalse(userRepository.create(u2))
    }

    @Test
    fun selects() {
        // all
        assertEquals(0, userRepository.all().count())
        assertEquals(0, userRepository.filter("points = ?", 0).count())
        for (i in 1..10) {
            val mario = User("$i", "mario$i", i, false)
            assertTrue(userRepository.create(mario))
        }

        assertEquals(10, userRepository.all().count())
        assertEquals(1, userRepository.filter("points = ?", 1).count())
        assertEquals(2, userRepository.filter("points > ?", 0, filters = filters(pageSize = 2)).count())
        assertEquals("mario1", userRepository.findOne("1")?.name)

        // OrderBy
        var result = userRepository.filter("points > ?", 0, filters = filters(orderBy = "points", desc = true)).toList()
        assertTrue(result.count() > 1)
        assertTrue(result[0].points > result[1].points)
        result = userRepository.filter("points > ?", 0, filters = filters(orderBy = "points")).toList()
        assertTrue(result.count() > 1)
        assertTrue(result[0].points < result[1].points)
    }


    @Test
    fun updates() {
        val user = User("ciao", "blabla", 10)
        assertTrue(userRepository.create(user))

        val user2 = user.copy(name = "blablaupdated")
        assertTrue(userRepository.update(user2, user2.id))

        val retrieved = userRepository.findOne(user.id)
        assertNotNull(retrieved)
        assertNotEquals(user.name, retrieved!!.name)
        assertEquals(user.id, retrieved.id)
        assertEquals(user2.id, retrieved.id)
        assertEquals(user2.name, retrieved.name)

        for (i in 1..10) {
            val mario = User("$i", "mario$i", i, false)
            assertTrue(userRepository.create(mario))
        }
        assertEquals(1, userRepository.filter("points = ?", 4).count())
        assertEquals(7,
            userRepository.updateWhere(mapOf(
                "points" to { i, stm -> stm.setInt(i, 4) }
            ),
                "points > ?", 4
            )
        )

        assertEquals(8, userRepository.filter("points = ?", 4).count())
    }

    @Test
    fun deletes() {
        for (i in 1..10) {
            val mario = User("$i", "mario$i", i, false)
            assertTrue(userRepository.create(mario))
        }

        assertEquals(10, userRepository.all().count())
        // check this with Debug
        assertEquals(
            10, userRepository.filter(
                "points > ?",
                -1,
                filters = filters(pageSize = 10)
            ).count()
        )
        assertTrue(userRepository.delete("1"))
        assertNull(userRepository.findOne("1"))
        assertEquals(9, userRepository.all().count())

        assertEquals(5, userRepository.deleteWhere("points > ?", 5))
        assertEquals(4, userRepository.all().count())
        assertEquals(4, userRepository.deleteWhere("points > ?", 0))
        assertEquals(0, userRepository.all().count())
    }

}