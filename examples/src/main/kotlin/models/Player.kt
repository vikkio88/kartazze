package org.vikkio.models

import io.github.vikkio88.kartazze.annotations.*

@Table("players")
data class Player(
    val name: String,
    val surname: String,
    val role: Role,
    val age: Int,
    val skill: Int,
    @Id
    val id: String = com.github.f4b6a3.ulid.UlidCreator.getUlid().toString(),
)

enum class Role {
    GOALKEEPER,
    DEFENDER,
    MIDFIELDER,
    STRICKER
}