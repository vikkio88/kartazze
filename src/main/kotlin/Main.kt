package org.vikkio

import org.vikkio.kartazze.ColumnMap
import org.vikkio.kartazze.DbHelper
import org.vikkio.kartazze.Entity
import org.vikkio.kartazze.annotations.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

data class User(
    @Id
    val id: String = "1",
    val name: String,
    val points: Int
)

class UserDao(connection: Connection) : Entity<User, String>(connection) {
    override val table: String = "user"
    override val primaryKey: String = "id"

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

fun main() {
    val connection = DriverManager.getConnection("jdbc:sqlite:test.db")
    val userDao = UserDao(connection)

    DbHelper.crateTableIfNotExists(connection, User::class)

    val us = userDao.filter("points >= ?", 0)
    us.forEach {
        println(it)
    }

}