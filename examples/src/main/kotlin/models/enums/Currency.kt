package org.vikkio.models.enums


enum class Currency(private val symbol: String, val conversionRate: Float) {
    EURO("€", 1.0f),
    US_DOLLAR("$", .92f),
    GB_POUND("£", 1.17f);

    override fun toString() = symbol
    fun getName() = name.lowercase()
}
