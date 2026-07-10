package com.liam.compose.entry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.liam.compose.features.home.navigation.homeEntries
import com.liam.compose.features.auth.navigation.authEntries
import com.liam.compose.features.settings.navigation.settingsEntries
import com.liam.compose.R
import kotlinx.serialization.Serializable

/**
 * App-specific navigation keys — one root key per bottom-nav tab.
 */

@Serializable
sealed interface ReportKey : NavKey {
    @Serializable
    data object Root : ReportKey
}

/**
 * Registers every destination the app can navigate to. Report is a placeholder until its real screen
 * exists. [backStack] is available for entries that need to navigate directly.
 */
fun EntryProviderScope<NavKey>.getAppEntries(backStack: NavBackStack<NavKey>) {
    authEntries(onLoginSuccess = {

    })
    homeEntries(backStack = backStack)
    entry<ReportKey.Root> { PlaceholderScreen(stringResource(R.string.tab_report)) }
    settingsEntries()
}


@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label)
    }
}
