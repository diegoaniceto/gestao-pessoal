package com.gestaopessoal.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gestaopessoal.app.ui.reminders.ReminderEditScreen
import com.gestaopessoal.app.ui.reminders.ReminderEditViewModel
import com.gestaopessoal.app.ui.reminders.ReminderListScreen

private object Routes {
    const val REMINDERS = "reminders"
    const val REMINDER_EDIT =
        "reminders/edit?${ReminderEditViewModel.ARG_REMINDER_ID}={${ReminderEditViewModel.ARG_REMINDER_ID}}"

    fun reminderEdit(id: Long = ReminderEditViewModel.NEW_REMINDER) =
        "reminders/edit?${ReminderEditViewModel.ARG_REMINDER_ID}=$id"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.REMINDERS) {
        composable(Routes.REMINDERS) {
            ReminderListScreen(
                onAddReminder = { navController.navigate(Routes.reminderEdit()) },
                onEditReminder = { id -> navController.navigate(Routes.reminderEdit(id)) },
            )
        }
        composable(
            route = Routes.REMINDER_EDIT,
            arguments = listOf(
                navArgument(ReminderEditViewModel.ARG_REMINDER_ID) {
                    type = NavType.LongType
                    defaultValue = ReminderEditViewModel.NEW_REMINDER
                },
            ),
        ) {
            ReminderEditScreen(onBack = { navController.popBackStack() })
        }
    }
}
