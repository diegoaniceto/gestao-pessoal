package com.gestaopessoal.app.ui.reminders

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestaopessoal.app.R
import com.gestaopessoal.app.data.Reminder
import com.gestaopessoal.app.data.ReminderRepository
import com.gestaopessoal.app.data.RepeatInterval
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class ReminderEditUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val date: LocalDate = defaultDateTime().toLocalDate(),
    val time: LocalTime = defaultDateTime().toLocalTime(),
    val repeat: RepeatInterval = RepeatInterval.NONE,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    @StringRes val error: Int? = null,
) {
    val isEditing: Boolean get() = id != 0L

    fun triggerAtMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDateTime.of(date, time).atZone(zone).toInstant().toEpochMilli()
}

/** Próxima hora cheia. */
private fun defaultDateTime(): LocalDateTime =
    LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS)

class ReminderEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ReminderRepository,
) : ViewModel() {

    private val reminderId: Long = savedStateHandle.get<Long>(ARG_REMINDER_ID) ?: NEW_REMINDER

    var uiState by mutableStateOf(ReminderEditUiState())
        private set

    init {
        if (reminderId != NEW_REMINDER) {
            viewModelScope.launch {
                repository.get(reminderId)?.let { uiState = it.toUiState() }
            }
        }
    }

    fun onTitleChange(value: String) {
        uiState = uiState.copy(title = value, error = null)
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value)
    }

    fun onDateChange(value: LocalDate) {
        uiState = uiState.copy(date = value, error = null)
    }

    fun onTimeChange(value: LocalTime) {
        uiState = uiState.copy(time = value, error = null)
    }

    fun onRepeatChange(value: RepeatInterval) {
        uiState = uiState.copy(repeat = value)
    }

    fun save(onSaved: () -> Unit) {
        val state = uiState
        val error = when {
            state.title.isBlank() -> R.string.error_title_required
            !state.isDone && state.triggerAtMillis() <= System.currentTimeMillis() ->
                R.string.error_date_in_past
            else -> null
        }
        if (error != null) {
            uiState = state.copy(error = error)
            return
        }
        viewModelScope.launch {
            repository.save(state.toReminder())
            onSaved()
        }
    }

    companion object {
        const val ARG_REMINDER_ID = "reminderId"
        const val NEW_REMINDER = -1L
    }
}

private fun Reminder.toUiState(zone: ZoneId = ZoneId.systemDefault()): ReminderEditUiState {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerAt), zone)
    return ReminderEditUiState(
        id = id,
        title = title,
        description = description,
        date = dateTime.toLocalDate(),
        time = dateTime.toLocalTime(),
        repeat = repeat,
        isDone = isDone,
        createdAt = createdAt,
    )
}

private fun ReminderEditUiState.toReminder() = Reminder(
    id = id,
    title = title.trim(),
    description = description.trim(),
    triggerAt = triggerAtMillis(),
    repeat = repeat,
    isDone = isDone,
    createdAt = createdAt,
)
