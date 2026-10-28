package com.gestaopessoal.app.notification

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gestaopessoal.app.GestaoPessoalApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * O Android apaga os alarmes ao reiniciar o aparelho ou atualizar o app,
 * então eles são recriados a partir do banco.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        val repository = (context.applicationContext as GestaoPessoalApp).container.reminderRepository
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.rescheduleAll()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
