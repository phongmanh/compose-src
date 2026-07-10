package com.liam.compose.core.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * The active [NavBackStack] for the current navigation host, provided by [AppNavigation].
 *
 * Lets screens navigate (e.g. `LocalNavBackStack.current.navigate(SomeKey)`) without having the
 * back stack threaded through every composable signature. Accessing it outside an [AppNavigation]
 * subtree throws.
 */
val LocalNavBackStack = compositionLocalOf<NavBackStack<NavKey>> {
    error("No NavBackStack provided. Wrap content in AppNavigation { ... }.")
}

/** Push [key] onto the back stack. */
fun NavBackStack<NavKey>.navigate(key: NavKey) {
    add(key)
}

/** Pop the top entry, if any. Returns the removed key or null when the stack is empty. */
fun NavBackStack<NavKey>.pop(): NavKey? = removeLastOrNull()

/** Pop everything above the root, leaving only the first (root) entry. */
fun NavBackStack<NavKey>.popToRoot() {
    while (size > 1) {
        removeAt(lastIndex)
    }
}
