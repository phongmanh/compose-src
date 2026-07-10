package com.liam.compose.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

/**
 * Generic Navigation 3 host. Renders [backStack] with [NavDisplay], applies the standard entry
 * decorators, and publishes the active back stack via [LocalNavBackStack] so descendants can
 * navigate without it being threaded through their signatures.
 *
 * Ensures navigation survives configuration changes and process death.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    backStack: NavBackStack<NavKey>,
    entries: EntryProviderScope<NavKey>.(backStack: NavBackStack<NavKey>) -> Unit,
) {
    CompositionLocalProvider(LocalNavBackStack provides backStack) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = { backStack.removeLastOrNull() },
            // Order matters: the saveable-state-holder must come first (or at least before any
            // decorator that relies on it) so each entry gets its own SaveableStateProvider —
            // that makes rememberSaveable work inside screen content and survive process death.
            // The viewmodel-store decorator then scopes ViewModels to the NavEntry's lifecycle
            // instead of the Activity.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entries(backStack)
            },
        )
    }
}
