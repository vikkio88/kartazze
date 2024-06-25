package io.github.vikkio88.kartazze


import kotlin.reflect.KClass

typealias RelationMapping = Iterable<Pair<KClass<out Any>, ColMap>>

data class ColMap(val externalColumn: String, val localColumn: String, val tableAlias: String? = null)

class Relation(
    private val mainClass: KClass<out Any>,
    private val models: RelationMapping,
    private val selects: Array<String>? = null,
) {
    val join: String by lazy {
        val mainTable = SchemaHelper.getTableName(mainClass)
        var result = ""
        for ((entity, idMap) in models) {
            val (extId, localId) = idMap
            val otherTableName = SchemaHelper.getTableName(entity)
            val otherTable = idMap.tableAlias ?: otherTableName
            result += "left join $otherTableName${if (idMap.tableAlias != null) " ${idMap.tableAlias}" else ""} on $otherTable.$extId = $mainTable.$localId "
        }

        result.trim()
    }

    val select: String by lazy {
        selects?.joinToString(", ")
            ?: models.joinToString(", ") {
                val (e, c) = it
                "${c.tableAlias ?: SchemaHelper.getTableName(e)}.*"
            }
    }

}