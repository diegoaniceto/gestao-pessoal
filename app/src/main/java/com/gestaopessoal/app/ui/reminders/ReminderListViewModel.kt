package com.gestaopessoal.app.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestaopessoal.app.data.Reminder
import com.gestaopessoal.app.data.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderListViewModel(
    private val repository: ReminderRepository,
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setDone(reminder: Reminder, done: Boolean) {
        viewModelScope.launch { repository.setDone(reminder.id, done) }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch { repository.delete(reminder) }
    }
}
