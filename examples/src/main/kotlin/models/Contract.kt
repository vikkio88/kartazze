package org.vikkio.models

import com.github.f4b6a3.ulid.UlidCreator
import io.github.vikkio88.kartazze.annotations.*
import java.time.Month

@Table(name = "contracts")
data class Contract(
    val durationMonths: Int,
    val startYear: Int,
    @ColumnType(type = Int::class)
    val startMonth: Month,
    val wage: Money,

    @References(
        externalTable = "teams",
        externalColumn = "id",
        externalIdType = String::class,
        columnName = "teamId"
    )
    val team: Team? = null,


    @References(
        externalTable = "players",
        externalColumn = "id",
        externalIdType = String::class,
        columnName = "playerId"
    )
    val player: Player? = null,

    @Id
    @Column(type = String::class, name = "cId")
    val id: String = UlidCreator.getUlid().toString(),
)