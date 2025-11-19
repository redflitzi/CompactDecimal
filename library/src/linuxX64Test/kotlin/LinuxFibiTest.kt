package io.github.redflitzi.compactdecimals

import kotlin.test.Test
import kotlin.test.assertEquals

class LinuxFibiTest {

    @Test
    fun test3rdElement() {
        assertEquals(8, generateFibi().take(3).last())
    }
}
