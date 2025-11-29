package io.github.redflitzi.compactdecimal

import Decimal
import io.github.redflitzi.*
import kotlin.test.Test
import kotlin.test.assertEquals


class UnaryOperatorsTest {

    @Test
    fun opPlusPlusTests() {
        var d: Decimal = Decimal(11)
        assertEquals(
            Decimal(11),
            d++,
            "op Dc++"
        )
        assertEquals(
            Decimal(13),
            ++d,
            "op ++Dc"
        )
    }
}