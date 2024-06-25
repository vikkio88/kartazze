package io.github.vikkio88.kartazze

import kotlin.test.Test
import kotlin.test.assertEquals

class RelationTest {

    @Test
    fun relationshipBuilderSimple() {
        val config = listOf(
            Player::class to ColMap("id", "playerId"),
            Team::class to ColMap("tId", "teamId")
        )

        val rel = Relation(Contract::class, config)

        assertEquals("left join player on player.id = contract.playerId left join team on team.tId = contract.teamId", rel.join)
        assertEquals("player.*, team.*", rel.select)
    }

    @Test
    fun relationshipBuilderAlias() {
        val config = listOf(
            Player::class to ColMap("id", "playerId", "pupu"),
            Team::class to ColMap("tId", "teamId")
        )

        val rel = Relation(Contract::class, config)

        assertEquals("left join player pupu on pupu.id = contract.playerId left join team on team.tId = contract.teamId", rel.join)
        assertEquals("pupu.*, team.*", rel.select)
    }
}

data class Contract(val id: String)
data class Player(val id: String)
data class Team(val id: String)