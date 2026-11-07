package com.gestaopessoal.app.ui.reminders

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gestaopessoal.app.GestaoPessoalApp
import com.gestaopessoal.app.R
import com.gestaopessoal.app.data.Reminder
import com.gestaopessoal.app.data.RepeatInterval
import com.gestaopessoal.app.notification.NotificationHelper
import com.gestaopessoal.app.ui.AppViewModelProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderListScreen(
    onAddReminder: () -> Unit,
    onEditReminder: (Long) -> Unit,
    viewModel: ReminderListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.reminders_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddReminder) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.reminder_add))
            }
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            PermissionBanners()

            if (reminders.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.reminders_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onClick = { onEditReminder(reminder.id) },
                            onDoneChange = { viewModel.setDone(reminder, it) },
                            onDelete = { pendingDelete = reminder },
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.reminder_delete_title)) },
            text = { Text(stringResource(R.string.reminder_delete_message, reminder.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(reminder)
                    pendingDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun ReminderItem(
    reminder: Reminder,
    onClick: () -> Unit,
    onDoneChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = reminder.isDone, onCheckedChange = onDoneChange)
            Column(Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (reminder.isDone) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (reminder.description.isNotBlank()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val repeatLabel = if (reminder.repeat != RepeatInterval.NONE) {
                    " · " + stringResource(reminder.repeat.labelRes())
                } else {
                    ""
                }
                Text(
                    text = formatDateTime(reminder.triggerAt) + repeatLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
            }
        }
    }
}

/** Avisa quando falta permissão para notificar ou para alarmes exatos. */
@Composable
private fun PermissionBanners() {
    val context = LocalContext.current
    val scheduler = remember {
        (context.applicationContext as GestaoPessoalApp).container.reminderScheduler
    }
    var hasNotificationPermission by remember { mutableStateOf(NotificationHelper.hasPermission(context)) }
    var canScheduleExact by remember { mutableStateOf(scheduler.canScheduleExact()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasNotificationPermission = granted }

    // Pede a permissão de notificação na primeira abertura (Android 13+).
    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Reavalia ao voltar das configurações do sistema.
    LifecycleResumeEffect(Unit) {
        hasNotificationPermission = NotificationHelper.hasPermission(context)
        canScheduleExact = scheduler.canScheduleExact()
        onPauseOrDispose { }
    }

    if (!hasNotificationPermission) {
        Banner(
            message = R.string.permission_notifications_message,
            action = R.string.permission_open_settings,
            onAction = {
                context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
                )
            },
        )
    }
    if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Banner(
            message = R.string.permission_exact_alarm_message,
            action = R.string.permission_open_settings,
            onAction = {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            },
        )
    }
}

@Composable
private fun Banner(@StringRes message: Int, @StringRes action: Int, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            TextButton(onClick = onAction, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(action))
            }
        }
    }
}

private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale.forLanguageTag("pt-BR"))

private fun formatDateTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(dateTimeFormatter)

@StringRes
fun RepeatInterval.labelRes(): Int = when (this) {
    RepeatInterval.NONE -> R.string.repeat_none
    RepeatInterval.DAILY -> R.string.repeat_daily
    RepeatInterval.WEEKLY -> R.string.repeat_weekly
    RepeatInterval.MONTHLY -> R.string.repeat_monthly
}
