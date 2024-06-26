package io.github.vikkio88.kartazze


import java.sql.ResultSet
import kotlin.reflect.KClass

typealias WithRelationMapping = Iterable<Pair<KClass<out Any>, WithColMap>>

data class WithColMap(val externalColumn: String, val localColumn: String, val tableAlias: String? = null)

typealias HasManyRelationMapping = Iterable<Pair<KClass<out Any>, HasColMap>>

data class HasColMap(val externalColumn: String, val localColumn: String, val assignFunction: (Any, ResultSet) -> Unit)


class WithRelation(
    private val mainClass: KClass<out Any>,
    private val models: WithRelationMapping,
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

class HasManyRelation(
    val models: HasManyRelationMapping
)