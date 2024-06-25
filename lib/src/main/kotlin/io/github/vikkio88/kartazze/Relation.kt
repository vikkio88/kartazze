package io.github.vikkio88.kartazze


import kotlin.reflect.KClass

typealias RelationMapping = Iterable<Pair<KClass<out Any>, Pair<String, String>>>

class Relation(
    private val mainClass: KClass<out Any>,
    private val models: RelationMapping,
    private val selects: Array<String>? = null,
    // Maybe add aliases
) {
    val join: String by lazy {
        val mainTable = SchemaHelper.getTableName(mainClass)
        var result = ""
        for ((entity, idMap) in models) {
            val (otherId, selfId) = idMap
            val otherTable = SchemaHelper.getTableName(entity)
            result += "left join on $otherTable.$otherId = $mainTable.$selfId "
        }

        result.trim()
    }

    val select: String by lazy {
        selects?.joinToString(", ")
            ?: models.joinToString(", ") {
                val (e, _) = it
                "${SchemaHelper.getTableName(e)}.*"
            }
    }

}