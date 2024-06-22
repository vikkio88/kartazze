package org.vikkio.models

import io.github.vikkio88.kartazze.annotations.*
import java.sql.Timestamp

@Table(name = "logs")
@Timestamps
data class LogEntry(
    val entry: String,
    @Id
    @AutoIncrement
    val id: Int = 0,
    @Ignore
    val createdAt: Timestamp? = null,
    @Ignore
    val updatedAt: Timestamp? = null
)