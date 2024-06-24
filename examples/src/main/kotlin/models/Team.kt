package org.vikkio.models

import com.github.f4b6a3.ulid.UlidCreator
import io.github.vikkio88.kartazze.annotations.*

@Table(name = "teams")
class Team(
    val name: String,
    @Id
    val id: String = UlidCreator.getUlid().toString()
)