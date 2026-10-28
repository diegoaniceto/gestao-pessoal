package com.gestaopessoal.app.data

import com.gestaopessoal.app.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow

/**
 * Ponto único para alterar lembretes: mantém o banco e os alarmes sempre em sincronia.
 */
class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler,
) {

    fun observeAll(): Flow<List<Reminder>> = dao.observeAll()

    suspend fun get(id: Long): Reminder? = dao.getById(id)

    suspend fun save(reminder: Reminder): Long {
        val id = if (reminder.id == 0L) {
            dao.insert(reminder)
        } else {
            dao.update(reminder)
            reminder.id
        }
        syncAlarm(reminder.copy(id = id))
        return id
    }

    suspend fun delete(reminder: Reminder) {
        scheduler.cancel(reminder.id)
        dao.delete(reminder)
    }

    suspend fun setDone(id: Long, done: Boolean) {
        val reminder = dao.getById(id) ?: return
        save(reminder.copy(isDone = done))
    }

    /**
     * "Concluir" pela notificação. Lembretes recorrentes já têm a próxima ocorrência
     * agendada, então só os únicos são marcados como concluídos.
     */
    suspend fun completeOccurrence(id: Long) {
        val reminder = dao.getById(id) ?: return
        if (reminder.repeat == RepeatInterval.NONE) save(reminder.copy(isDone = true))
    }

    /**
     * Chamado quando o alarme dispara. Se o lembrete se repete, já agenda a próxima
     * ocorrência. Retorna o lembrete como estava no disparo (para montar a notificação).
     */
    suspend fun onReminderFired(id: Long): Reminder? {
        val reminder = dao.getById(id) ?: return null
        if (reminder.isDone) return null
        reminder.repeat.nextOccurrence(reminder.triggerAt)?.let { next ->
            save(reminder.copy(triggerAt = next))
        }
        return reminder
    }

    /** Recria todos os alarmes (após reiniciar o aparelho, atualizar o app etc.). */
    suspend fun rescheduleAll() {
        val now = System.currentTimeMillis()
        dao.getActive().forEach { reminder ->
            if (reminder.triggerAt > now) {
                scheduler.schedule(reminder)
            } else {
                reminder.repeat.nextOccurrence(reminder.triggerAt, now)?.let { next ->
                    save(reminder.copy(triggerAt = next))
                }
            }
        }
    }

    private fun syncAlarm(reminder: Reminder) {
        if (!reminder.isDone && reminder.triggerAt > System.currentTimeMillis()) {
            scheduler.schedule(reminder)
        } else {
            scheduler.cancel(reminder.id)
        }
    }
}
