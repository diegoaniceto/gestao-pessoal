package com.gestaopessoal.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    /** Momento do disparo, em epoch millis. */
    val triggerAt: Long,
    val repeat: RepeatInterval = RepeatInterval.NONE,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
