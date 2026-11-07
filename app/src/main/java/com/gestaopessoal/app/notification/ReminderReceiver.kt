package com.gestaopessoal.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gestaopessoal.app.GestaoPessoalApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Recebe o disparo do alarme e a ação "Concluir" da notificação. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId < 0) return

        val repository = (context.applicationContext as GestaoPessoalApp).container.reminderRepository
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_FIRE -> repository.onReminderFired(reminderId)?.let {
                        NotificationHelper.showReminder(context, it)
                    }

                    ACTION_COMPLETE -> {
                        repository.completeOccurrence(reminderId)
                        NotificationHelper.cancel(context, reminderId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.gestaopessoal.app.action.REMINDER_FIRE"
        const val ACTION_COMPLETE = "com.gestaopessoal.app.action.REMINDER_COMPLETE"
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
