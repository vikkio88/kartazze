package io.github.vikkio88.kartazze

import kotlin.test.Test
import kotlin.test.assertEquals

class RelationTest {

    @Test
    fun relationshipBuilder() {
        val config = listOf(
            Player::class to ("id" to "playerId"),
            Team::class to ("tId" to "teamId")
        )

        val rel = Relation(Contract::class, config)

        assertEquals("left join on player.id = contract.playerId left join on team.tId = contract.teamId", rel.join)
        assertEquals("player.*, team.*", rel.select)
    }
}

data class Contract(val id: String)
data class Player(val id: String)
data class Team(val id: String)