package com.gestaopessoal.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gestaopessoal.app.GestaoPessoalApp
import com.gestaopessoal.app.ui.reminders.ReminderEditViewModel
import com.gestaopessoal.app.ui.reminders.ReminderListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            ReminderListViewModel(app().container.reminderRepository)
        }
        initializer {
            ReminderEditViewModel(createSavedStateHandle(), app().container.reminderRepository)
        }
    }
}

private fun CreationExtras.app(): GestaoPessoalApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GestaoPessoalApp
