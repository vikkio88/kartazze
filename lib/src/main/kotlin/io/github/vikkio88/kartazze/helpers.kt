package io.github.vikkio88.kartazze

import java.sql.PreparedStatement

typealias ColumnMap = Map<String, (Int, PreparedStatement) -> Unit>

fun columnMapOf(vararg pairs: Pair<String, () -> Any>): ColumnMap {
    val map = mutableMapOf<String, (Int, PreparedStatement) -> Unit>()
    for ((column, accessor) in pairs) {
        map[column] = { i, stm -> stm.setObject(i, accessor()) }
    }

    return map
}