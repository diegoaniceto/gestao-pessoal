package com.gestaopessoal.app

import android.app.Application
import android.content.Context
import com.gestaopessoal.app.data.AppDatabase
import com.gestaopessoal.app.data.ReminderRepository
import com.gestaopessoal.app.notification.NotificationHelper
import com.gestaopessoal.app.notification.ReminderScheduler

class GestaoPessoalApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannels(this)
    }
}

/** Injeção de dependências manual. Pode virar Hilt quando o app crescer. */
class AppContainer(context: Context) {
    private val database by lazy { AppDatabase.getInstance(context) }
    val reminderScheduler by lazy { ReminderScheduler(context) }
    val reminderRepository by lazy {
        ReminderRepository(database.reminderDao(), reminderScheduler)
    }
}
