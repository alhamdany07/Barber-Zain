package com.example

import com.example.ui.components.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testRupiahFormatting() {
        val formatted = Formatters.formatRupiah(50000.0)
        assertEquals("Rp 50.000", formatted)

        val formattedLarge = Formatters.formatRupiah(1250000.0)
        assertEquals("Rp 1.250.000", formattedLarge)
    }

    @Test
    fun testQueueFormatting() {
        val seq = 3
        val queueStr = String.format(java.util.Locale.US, "A%03d", seq)
        assertEquals("A003", queueStr)
    }
}
