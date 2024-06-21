package org.vikkio

import io.github.vikkio88.kartazze.SchemeHelper
import org.vikkio.models.Player
import org.vikkio.models.Role
import org.vikkio.repos.PlayerRepo
import java.sql.Connection
import java.sql.DriverManager


fun main() {
    val conn: Connection = DriverManager.getConnection("jdbc:sqlite::memory:")
    SchemeHelper.crateTableIfNotExists(conn, Player::class)
    val p = PlayerRepo(conn)

    p.create(Player("Mario", "Balotelli", Role.STRICKER, 30, 70))

    for (r in p.all()) {
        println(r)
    }
}