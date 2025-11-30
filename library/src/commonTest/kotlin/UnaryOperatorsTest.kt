package io.github.redflitzi.compactdecimal

import io.github.redflitzi.compactdecimal.*

import kotlin.test.Test
import kotlin.test.assertEquals


class UnaryOperatorsTest {

    @Test
    fun opPlusPlusTests() {
        var d: Decimal = Decimal(11)
        assertEquals(
            Decimal(11),
            d++,
            "op Dc++" // ++ happens after evaluation
        )
        assertEquals(
            Decimal(13),
            ++d,
            "op ++Dc" // ++ happens before evaluation
        )
    }

    @Test
    fun opMinusMinusTests() {
        var d: Decimal = Decimal(11)
        assertEquals(
            Decimal(11),
            d--,
            "op Dc--"
        )
        assertEquals(
            Decimal(9),
            --d,
            "op --Dc"
        )
    }


    @Test
    fun absTests() {
        var d: Decimal = Decimal(-11)
        assertEquals(
            Decimal(11),
            Decimal.abs(d),
            "abs(-11.Dc)"
        )
        assertEquals(
            1.23456789.Dc,
            Decimal.abs(-1.23456789.Dc),
            "abs(-1.23456789.Dc)"
        )
        assertEquals(
            1.23456789.Dc,
            abs((-1.23456789).Dc),
            "abs(-1.23456789.Dc)"
        )

    }



}