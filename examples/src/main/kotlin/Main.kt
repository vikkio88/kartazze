package org.vikkio

import io.github.vikkio88.kartazze.SchemeHelper
import org.vikkio.models.LogEntry
import org.vikkio.models.Player
import org.vikkio.models.Role
import org.vikkio.repos.LogRepo
import org.vikkio.repos.PlayerRepo
import java.sql.Connection
import java.sql.DriverManager


fun main() {
    val conn: Connection = DriverManager.getConnection("jdbc:sqlite:example.test.db")
    SchemeHelper.crateTableIfNotExists(conn, Player::class)
    SchemeHelper.crateTableIfNotExists(conn, LogEntry::class)
    val p = PlayerRepo(conn)
    p.create(Player("Mario", "Balotelli", Role.STRIKER, 30, 70))
    p.create(Player("Carlos", "Tevez", Role.STRIKER, 35, 62, "Apache"))

    for (r in p.all()) {
        println(r)
    }


    val l = LogRepo(conn)
    l.create(LogEntry(entry = "test1"))
    l.create(LogEntry(entry = "test2"))
    for(log in l.all()){
        println(log)
    }
}