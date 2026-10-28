package com.gestaopessoal.app.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gestaopessoal.app.data.Reminder

class ReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    /** No Android 12+ o usuário pode negar alarmes exatos; aí usamos um alarme aproximado. */
    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    @SuppressLint("ScheduleExactAlarm") // verificado em canScheduleExact()
    fun schedule(reminder: Reminder) {
        val pendingIntent = pendingIntent(reminder.id)
        try {
            if (canScheduleExact()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAt,
                    pendingIntent,
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAt,
                    pendingIntent,
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAt,
                pendingIntent,
            )
        }
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntent(reminderId))
    }

    private fun pendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_FIRE
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
