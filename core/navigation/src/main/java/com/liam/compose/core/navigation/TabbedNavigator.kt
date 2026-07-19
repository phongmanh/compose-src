package com.liam.compose.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Owns one [NavBackStack] per tab and tracks the selected tab, encapsulating the multi-back-stack
 * bottom-navigation pattern. [select] switches to a different tab, or pops the current tab's stack
 * back to its root when the already-selected tab is re-selected.
 *
 * Create instances with [rememberTabbedNavigator] rather than directly.
 */
@Stable
class TabbedNavigator<T>(
    val tabs: List<T>,
    private val backStacks: Map<T, NavBackStack<NavKey>>,
    private val selectedTabState: MutableState<T>,
) {
    var selectedTab: T
        get() = selectedTabState.value
        private set(value) {
            selectedTabState.value = value
        }

    /** Back stack of the currently selected tab. */
    val currentBackStack: NavBackStack<NavKey>
        get() = backStackFor(selectedTab)

    fun backStackFor(tab: T): NavBackStack<NavKey> =
        backStacks[tab] ?: error("No back stack registered for tab $tab")

    /** Select [tab]; re-selecting the current tab pops its stack to root. */
    fun select(tab: T) {
        if (tab == selectedTab) {
            currentBackStack.popToRoot()
        } else {
            selectedTab = tab
        }
    }
}

/**
 * Remembers a [TabbedNavigator] with one back stack per entry in [tabs], each seeded with the root
 * key from [rootKeyFor]. The back stacks and the selected tab are saved across configuration
 * changes and process death ([tabSaver] serializes the selected tab).
 */
@Composable
fun <T> rememberTabbedNavigator(
    tabs: List<T>,
    rootKeyFor: (T) -> NavKey,
    initialTab: T,
    tabSaver: Saver<T, out Any>,
): TabbedNavigator<T> {
    val backStacks = tabs.associateWith { tab ->
        key(tab) { rememberNavBackStack(rootKeyFor(tab)) }
    }
    val selectedTabState = rememberSaveable(stateSaver = tabSaver) {
        mutableStateOf(initialTab)
    }
    return remember(tabs) {
        TabbedNavigator(tabs, backStacks, selectedTabState)
    }
}
