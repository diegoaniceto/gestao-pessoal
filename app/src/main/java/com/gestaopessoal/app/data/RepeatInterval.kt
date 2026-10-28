package com.gestaopessoal.app.data

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

enum class RepeatInterval {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY;

    /**
     * Próxima ocorrência estritamente depois de [now], partindo de [from].
     * Retorna null quando o lembrete não se repete.
     */
    fun nextOccurrence(
        from: Long,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): Long? {
        if (this == NONE) return null
        val start = ZonedDateTime.ofInstant(Instant.ofEpochMilli(from), zone)
        var step = 1L
        var next = advance(start, step)
        while (next.toInstant().toEpochMilli() <= now) {
            step++
            next = advance(start, step)
        }
        return next.toInstant().toEpochMilli()
    }

    // Sempre calcula a partir da data original, para não "escorregar" o dia
    // (ex.: dia 31 mensal continua caindo no último dia do mês).
    private fun advance(start: ZonedDateTime, times: Long): ZonedDateTime = when (this) {
        DAILY -> start.plusDays(times)
        WEEKLY -> start.plusWeeks(times)
        MONTHLY -> start.plusMonths(times)
        NONE -> start
    }
}
