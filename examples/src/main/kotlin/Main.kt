package org.vikkio

import io.github.vikkio88.kartazze.HasColMap
import io.github.vikkio88.kartazze.HasManyRelation
import io.github.vikkio88.kartazze.SchemaHelper
import org.vikkio.models.*
import org.vikkio.models.enums.Currency
import org.vikkio.repos.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Month


fun main() {
    val conn: Connection = DriverManager.getConnection("jdbc:sqlite:example.test.db")
    migrate(conn)
    playersInsert(conn)


//    c.findOne("01J14X9EJ75BN49P0JJFBDB205")?.let {
//        println(it)
//    }

//    logsTests(conn)

}

private fun migrate(conn: Connection) {
    SchemaHelper.crateTableIfNotExists(conn, Player::class)
    SchemaHelper.crateTableIfNotExists(conn, Team::class)
    SchemaHelper.crateTableIfNotExists(conn, Contract::class)
    SchemaHelper.crateTableIfNotExists(conn, LogEntry::class)
}

private fun playersInsert(conn: Connection) {
    val p = PlayerRepo(conn)
    p.truncate()
    val c = ContractRepo(conn)
    c.truncate()
    val t = TeamRepo(conn)
    t.truncate()

    p.create(Player("Mario", "Balotelli", Role.STRIKER, 30, 70))
    p.create(Player("Carlos", "Tevez", Role.STRIKER, 35, 62, "Apache"))
    p.create(Player("Carlos", "Mario", Role.STRIKER, 35, 62, "Apache"))
    p.create(Player("Carlos", "Ciccio", Role.STRIKER, 35, 62, "Apache"))


    val nt = Team("Juventus")
    t.create(nt)


    val ids = mutableListOf<String>()
    for (r in p.all()) {
        println(r)
        val nc = Contract(
            durationMonths = 6,
            startYear = 2020,
            startMonth = Month.JANUARY,
            wage = Money(10000000, Currency.EURO),
            player = r,
            team = nt,
        )
        c.create(
            nc
        )

        ids.add(nc.id)
    }


    val resses = c.withTeamAndPlayer().all()
    for (res in resses)
        println(res)

    val contractMapper = ContractMapper()
    val cc = t.findOneWith(
        HasManyRelation(
            listOf(
                Contract::class to
                        HasColMap("teamId", "tId", fun(main: Any, children: ResultSet) {
                            if (main !is Team) return
                            val result = mutableListOf<Contract>()
                            while (children.next()) {
                                result.add(contractMapper.mapResultSetToEntity(children))
                            }

                            main.contracts = result
                        })
            )
        ), nt.id
    )

    println(cc)
//    t.filterWith(HasManyRelation())
}

private fun logsTests(conn: Connection) {
    val l = LogRepo(conn)
    l.create(LogEntry(entry = "test1"))
    l.create(LogEntry(entry = "test2"))
    for (log in l.all()) {
        println(log)
    }

    l.findOne(1)?.let {
        val newOne = it.copy(entry = "updated")
        println("result: ${l.update(newOne, newOne.id)}")
    }
    println("After")
    for (log in l.all()) {
        println(log)
    }
}