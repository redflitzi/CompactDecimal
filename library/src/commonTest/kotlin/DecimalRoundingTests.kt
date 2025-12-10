package io.github.redflitzi.compactdecimal

import io.github.redflitzi.compactdecimal.Decimal.Companion.getRoundingMode
import kotlin.test.Test
import kotlin.test.assertEquals

// Tests according to:
// https://developer.android.com/reference/java/math/RoundingMode

class Rounding_UP_Tests {

    @Test
    fun decimalRoundingTests_UP() {
        Decimal.setRoundingMode(Decimal.RoundingMode.UP)
        //Decimal.setPrecision(0)
        assertEquals(
            "5600",
            "5555.12".Dc.setScale(-2).toPlainString(),
            "round(-2)  5555.12 => 5600"
        )
        assertEquals(
            "5100",
            "5000.02".Dc.setScale(-2).toPlainString(),
            "round(-2)  5000.02 => 5100"
        )
        assertEquals(
            "5000",
            "5000.00".Dc.setScale(-2).toPlainString(),
            "round(-2)  5000.00 => 5000"
        )
        assertEquals(
            "6",
            "5.5".Dc.setScale(0).toPlainString(),
            "operator 5.5=> 6"
        )
        assertEquals(
            "-5600",
            "-5555.12".Dc.setScale(-2).toPlainString(),
            "round(-2)  -5555.12 => -5600"
        )
        assertEquals(
            "-5100",
            "-5000.02".Dc.setScale(-2).toPlainString(),
            "round(-2)  -5000.02 => -5100"
        )
        assertEquals(
            "-5000",
            "-5000.00".Dc.setScale(-2).toPlainString(),
            "round(-2)  -5000.00 => -5000"
        )
        assertEquals(
            "-6",
            "-5.5".Dc.setScale(0).toPlainString(),
            "operator -5.5=> -6"
        )
        assertEquals(
            "3",
            "2.5".Dc.setScale(0).toPlainString(),
            "operator 2.5 => 3"
        )
        assertEquals(
            "2",
            "1.6".Dc.setScale(0).toPlainString(),
            "operator 1.6 => 2"
        )
        assertEquals(
            "2",
            "1.1".Dc.setScale(0, Decimal.RoundingMode.UP).toPlainString(),
            "operator 1.1 => 2, ${getRoundingMode()}"
        )
        assertEquals(
            "1",
            "1.0".Dc.setScale(0).toPlainString(),
            "operator 1.0 => 1, ${getRoundingMode()}"
        )
    }
}

class ArithmeticRoundingTest {

    @Test
    fun opRoundingTests() {
        //var d: Decimal("13.7777")
        assertEquals(
            "13.778",
            "13.7777".Dc.setScale(3).toPlainString(),
            "operator 13.7777.Dc.setScale(3)"
        )

        /*

        d=13.7777.Dc.roundHalfEven()
        assertEquals(
            "13.778",
            d.roundHalfEven().toPlainString(),
            "operator roundingHalfEven(13.7777.Dc)"
        )

        d=13.7777.Dc.roundHalfEven()
        assertEquals(
            "13.8",
            d.roundHalfEven(1).toPlainString(),
            "operator roundingHalfEven(13.7777.Dc)"
        )

        d=12.7777.Dc
        assertEquals(
            "12.8",
            d.roundHalfEven(1).toPlainString(),
            "operator roundingHalfEven(12.7777.Dc)"
        )

        d=12.7777.Dc
        assertEquals(
            "12",
            d.roundHalfEven(0).toPlainString(),
            "operator roundingHalfEven(12.7777.Dc)"
        )

        d=13.7777.Dc
        assertEquals(
            "14",
            d.roundHalfEven(0).toPlainString(),
            "operator roundingHalfEven(12.7777.Dc)"
        )

         */
    }

}