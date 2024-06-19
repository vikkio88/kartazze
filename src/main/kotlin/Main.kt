package org.vikkio

import org.vikkio.kartazze.ColumnMap
import org.vikkio.kartazze.Repository
import org.vikkio.kartazze.SchemeHelper
import org.vikkio.kartazze.annotations.Id
import org.vikkio.kartazze.annotations.Table
import org.vikkio.kartazze.columnMapOf

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

@Table("users")
data class User(
    @Id
    val id: String = "1",
    val name: String,
    val points: Int
)

class UserRepo(connection: Connection) : Repository<User, String>(connection, User::class) {
    override fun mapResultSetToEntity(rs: ResultSet) = User(
        id = rs.getString("id"),
        name = rs.getString("name"),
        points = rs.getInt("points")
    )

    override fun mapEntityToColumns(obj: User): ColumnMap {
        return columnMapOf(
            "id" to { obj.id },
            "name" to { obj.name },
            "points" to { obj.points }
        )

        //Or
//        return mapOf(
//            "id" to { index, stm -> stm.setString(index, obj.id) },
//            "name" to { index, stm -> stm.setString(index, obj.name) },
//            "points" to { index, stm -> stm.setInt(index, obj.points) },
//        )
    }

}

fun main() {
    val connection = DriverManager.getConnection("jdbc:sqlite:test.db")
    val userRepo = UserRepo(connection)

    SchemeHelper.crateTableIfNotExists(connection, User::class)
    for (i in 1..10) {
        userRepo.create(User("id-$i", "Mario $i", i))
    }

    val us = userRepo.filter("points >= ?", 0)
    us.forEach {
        println(it)
    }

}