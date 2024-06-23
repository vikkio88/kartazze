package org.vikkio.models

import kotlinx.serialization.Serializable
import org.vikkio.models.enums.Currency

const val MULTIPLIER: Float = 100.0f

@Serializable
data class Money(val value: Int, val currency: Currency) {
    val amount: Float get() = value / MULTIPLIER

    override fun toString(): String {
        return "${"%.2f".format(amount)}$currency"
    }

    operator fun plus(other: Money): Money {
        var otherAmount = other
        if (other.currency != this.currency) {
            otherAmount = other.convert(currency)
        }

        return Money(this.value + otherAmount.value, this.currency)
    }

    operator fun minus(other: Money): Money {
        var otherAmount = other
        if (other.currency != this.currency) {
            otherAmount = other.convert(currency)
        }

        if (otherAmount.value > this.value) {
            throw UnsupportedOperationException("Cannot subtract more than the value")
        }

        return Money(this.value - otherAmount.value, this.currency)
    }

    operator fun times(i: Int): Money {
        return Money(value * i, currency)
    }

    fun adjustPercentage(i: Float): Money {
        val newValue = value + (value * i)
        return Money(newValue.toInt(), currency)
    }

    fun convert(currency: Currency): Money {
        val newValue = value * this.currency.conversionRate / currency.conversionRate
        return Money(newValue.toInt(), currency)
    }

    //<editor-fold desc="Comparable">
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Money -> other.value == this.value && other.currency == this.currency
            else -> false
        }
    }

    override fun hashCode(): Int {
        return value + currency.hashCode()
    }
    //</editor-fold>
}