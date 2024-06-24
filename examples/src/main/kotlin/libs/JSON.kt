package org.vikkio.libs

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json

// This is to take out the "type"
val format = Json {
    classDiscriminatorMode = ClassDiscriminatorMode.NONE
    ignoreUnknownKeys = true
}
//val format = Json

object JSON {
    inline fun <reified T> stringify(o: T): String {
        return format.encodeToString(o)
    }

    inline fun <reified T> parse(str: String): T {
        return Json.decodeFromString<T>(str)
    }
}