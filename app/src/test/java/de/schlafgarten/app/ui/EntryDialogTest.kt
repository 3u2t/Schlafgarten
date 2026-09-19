package de.schlafgarten.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class EntryDialogTest {

    @Test
    fun `overnight maps bed to previous day`() {
        val (error, entry) = parseEntry("2026-09-17", "23:00", "07:00")
        assertNull(error)
        assertNotNull(entry)
        assertEquals("2026-09-17", entry!!.wakeDate)
        assertEquals("2026-09-16T23:00:00", entry.bedTime)
        assertEquals("2026-09-17T07:00:00", entry.wakeTime)
    }

    @Test
    fun `same day nap stays on same day`() {
        val (error, entry) = parseEntry("2026-09-17", "14:00", "15:00")
        assertNull(error)
        assertNotNull(entry)
        assertEquals("2026-09-17T14:00:00", entry!!.bedTime)
        assertEquals("2026-09-17T15:00:00", entry.wakeTime)
    }

    @Test
    fun `invalid date returns error not entry`() {
        val (error, entry) = parseEntry("17.09.2026", "23:00", "07:00")
        assertNotNull(error)
        assertEquals(null, entry)
    }

    @Test
    fun `invalid time returns error not entry`() {
        val (error, entry) = parseEntry("2026-09-17", "25:00", "07:00")
        assertNotNull(error)
        assertEquals(null, entry)
    }
}
