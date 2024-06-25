package org.vikkio.models

import com.github.f4b6a3.ulid.UlidCreator
import io.github.vikkio88.kartazze.annotations.*

@Table(name = "teams")
data class Team(
    @ColumnName(name = "tName")
    val name: String,
    @Id
    @Column(type = String::class, name = "tId")
    val id: String = UlidCreator.getUlid().toString()
)