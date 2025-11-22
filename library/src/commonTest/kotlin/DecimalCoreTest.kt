package io.github.redflitzi.compactdecimals

import toDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalCoreTest {

    @Test fun intConstructorTests() {
        assertEquals(
            "15",
            Decimal(15).toPlainString(),
            "intConstructor: 15"
        )
        assertEquals(
            "15",
            Decimal(15L).toPlainString(),
            "intConstructor: 15L"
        )
    }

    @Test fun floatConstructorTests() {

        assertEquals(
            "100000.47",
            Decimal(100000.47F).toPlainString(),
            "floatConstructor: 0000000.47"
        )
        assertEquals(
            "10000000.47",
            Decimal(10000000.47).toPlainString(),
            "doubleConstructor: 0000000.47"
        )
        assertEquals(
            "10000000.47",
            (10000000.47).toDecimal().toPlainString(),
            "double.toDecimal(): 0000000.47"
        )
        assertEquals(
            "15.3",
            (15.3F).toString(),
            "Float Test 15.3F"
        )
        assertEquals(
            "15.3",
            (15.3F).toDecimal().toPlainString(),
            "float.toDecimal(): 15.3F"
        )

    }

    @Test fun stringConstructorTests() {
        assertEquals(
            "123",
            Decimal("123").toPlainString(),
            "stringConstructor: 123"
            )
        assertEquals(
            "123000",
            Decimal("123000").toPlainString(),
            "stringConstructor: 123000"
        )
        assertEquals(
            "123",
            Decimal("123.000").toPlainString(),
            "stringConstructor: 123.000"
        )
        assertEquals(
            "123.4",
            Decimal("123.4").toPlainString(),
            "stringConstructor: 123.4"
        )
        assertEquals(
            "-123.004",
            Decimal("-123.004").toPlainString(),
            "stringConstructor: -123.004"
        )
        assertEquals(
            "1.234",
            Decimal("1.234E0").toPlainString(),
            "stringConstructor: 1.234E0"
        )
        assertEquals(
            "123.4",
            Decimal("1.234E2").toPlainString(),
            "stringConstructor: 1.234E2"
        )
        assertEquals(
            "-123.4",
            Decimal("-1.234E2").toPlainString(),
            "stringConstructor: -1.234E2"
        )
        assertEquals(
            "0.01234",
            Decimal("1.234E-2").toPlainString(),
            "stringConstructor: 1.234E-2"
        )

    }

    @Test fun toPlainStringTests() {
        assertEquals(
            "123",
            Decimal(123L, 0).toPlainString(),
            "toPlainString: +mantisse, 0 places"
        )
        assertEquals(
            "1.24",
            Decimal(124L, 2).toPlainString(),
            "toPlainString: +mantisse, +places"
        )
        assertEquals(
            "12500",
            Decimal(125L, -2).toPlainString(),
            "toPlainString: +mantisse, -places"
        )
        assertEquals(
            "-125",
            Decimal(-125L, 0).toPlainString(),
            "toPlainString: -mantisse, 0 places"
        )
        assertEquals(
            "-1.25",
            Decimal(-125L, +2).toPlainString(),
            "toPlainString: -mantisse, +places"
        )
        assertEquals(
            "-12500",
            Decimal(-125L, -2).toPlainString(),
            "toPlainString: -mantisse, -places"
        )
    }

    @Test fun toScientificStringTests() {
        assertEquals(
            "1.23E2",
            Decimal(123L, 0).toScientificString(),
            "toScientific: +mantisse, 0 places"
        )
        assertEquals(
            "1.24E-8",
            Decimal(124L, 10).toScientificString(),
            "toScientific: +mantisse, +places"
        )
        assertEquals(
            "1.25E4",
            Decimal(125L, -2).toScientificString(),
            "toScientific: +mantisse, -places"
        )
        assertEquals(
            "-1.25E2",
            Decimal(-125L, 0).toScientificString(),
            "toScientific: -mantisse, 0places"
        )
        assertEquals(
            "-1.25E-8",
            Decimal(-125L, 10).toScientificString(),
            "toScientific: -mantisse, +places"
        )
        assertEquals(
            "-1.25E12",
            Decimal(-125L, -10).toScientificString(),
            "toScientific: -mantisse, -places"
        )

    }
}
