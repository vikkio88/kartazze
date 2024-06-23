package org.vikkio.models

import io.github.vikkio88.kartazze.annotations.*
import java.time.Month

class Contract(
    @Id
    val id: String,
    val durationMonths: Int,
    val startYear: Int,
    val startMonth: Month,
    val wage: Money,

    @References(
        externalTable = "teams",
        externalColumn = "id",
        externalIdType = String::class,
        columnName = "teamId"
    )
    val team: Team,
    @References(
        externalTable = "players",
        externalColumn = "id",
        externalIdType = String::class,
        columnName = "playerId"
    )
    val player: Player
)