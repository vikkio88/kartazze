package org.vikkio.models

import io.github.vikkio88.kartazze.annotations.*
import java.sql.Timestamp

@Table(name = "logs")
@Timestamps
data class LogEntry(
    val entry: String,
    @Id
    @AutoIncrement
    val id: Int,
    @Ignore
    val createdAt: Timestamp,
    @Ignore
    val updatedAt: Timestamp
) {

    constructor(entry: String) : this(
        entry,
        0,
        Timestamp(System.currentTimeMillis()),
        Timestamp(System.currentTimeMillis())
    )
}