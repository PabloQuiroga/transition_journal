package com.refugio.journal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.refugio.journal.ui.editor.EntryEditorScreen
import com.refugio.journal.ui.editor.EntryEditorViewModel
import com.refugio.journal.ui.lock.LockScreen
import com.refugio.journal.ui.lock.LockViewModel
import com.refugio.journal.ui.timeline.TimelineScreen
import com.refugio.journal.ui.timeline.TimelineViewModel

sealed class Screen(val route: String) {
    data object Lock : Screen("lock")
    data object Timeline : Screen("timeline")
    data object Editor : Screen("editor")
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Lock.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Lock.route) {
            val viewModel: LockViewModel = hiltViewModel()
            LockScreen(
                viewModel = viewModel,
                onUnlocked = {
                    navController.navigate(Screen.Timeline.route) {
                        popUpTo(Screen.Lock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Timeline.route) {
            val viewModel: TimelineViewModel = hiltViewModel()
            TimelineScreen(
                viewModel = viewModel,
                onNewEntryClick = {
                    navController.navigate(Screen.Editor.route)
                },
                onEntryClick = { entryId ->
                    // Navigation to detail or editor in future version
                }
            )
        }

        composable(Screen.Editor.route) {
            val viewModel: EntryEditorViewModel = hiltViewModel()
            EntryEditorScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
