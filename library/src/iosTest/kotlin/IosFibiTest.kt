package io.github.redflitzi.compactdecimals

import kotlin.test.Test
import kotlin.test.assertEquals

class IosFibiTest {

    @Test
    fun test3rdElement() {
        assertEquals(7, generateFibi().take(3).last())
    }
}