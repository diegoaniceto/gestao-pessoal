package com.gestaopessoal.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class RepeatIntervalTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    private fun millis(text: String): Long =
        LocalDateTime.parse(text).atZone(zone).toInstant().toEpochMilli()

    @Test
    fun noneDoesNotRepeat() {
        assertNull(RepeatInterval.NONE.nextOccurrence(millis("2026-10-27T09:00"), zone = zone))
    }

    @Test
    fun dailyGoesToNextDayAtSameTime() {
        val next = RepeatInterval.DAILY.nextOccurrence(
            from = millis("2026-10-27T09:00"),
            now = millis("2026-10-27T09:00"),
            zone = zone,
        )
        assertEquals(millis("2026-10-28T09:00"), next)
    }

    @Test
    fun dailySkipsMissedOccurrences() {
        val next = RepeatInterval.DAILY.nextOccurrence(
            from = millis("2026-10-20T09:00"),
            now = millis("2026-10-27T10:00"),
            zone = zone,
        )
        assertEquals(millis("2026-10-28T09:00"), next)
    }

    @Test
    fun weeklyKeepsWeekday() {
        val next = RepeatInterval.WEEKLY.nextOccurrence(
            from = millis("2026-10-27T18:30"),
            now = millis("2026-10-27T18:30"),
            zone = zone,
        )
        assertEquals(millis("2026-11-03T18:30"), next)
    }

    @Test
    fun monthlyOnDay31DoesNotDrift() {
        val from = millis("2026-01-31T08:00")
        val feb = RepeatInterval.MONTHLY.nextOccurrence(from, now = from, zone = zone)
        assertEquals(millis("2026-02-28T08:00"), feb)

        val mar = RepeatInterval.MONTHLY.nextOccurrence(from, now = feb!!, zone = zone)
        assertEquals(millis("2026-03-31T08:00"), mar)
    }
}
