package com.liam.compose.features.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.liam.compose.core.navigation.LocalNavBackStack
import com.liam.compose.core.navigation.pop
import com.liam.compose.features.auth.ui.screens.ChangePasswordScreen
import com.liam.compose.features.settings.ui.SettingsScreen
import kotlinx.serialization.Serializable

/**
 * Navigation keys for the Settings tab — its root plus the change-password destination.
 */
@Serializable
sealed interface SettingKey : NavKey {
    @Serializable
    data object Root : SettingKey

    @Serializable
    data class ChangePassword(val username: String) : SettingKey
}

/** Registers every Settings destination into the app's entry provider. */
fun EntryProviderScope<NavKey>.settingsEntries() {
    entry<SettingKey.Root> { SettingsScreen() }
    entry<SettingKey.ChangePassword> { key ->
        val navBackStack = LocalNavBackStack.current
        ChangePasswordScreen(
            username = key.username,
            onSuccess = { navBackStack.pop() },
        )
    }
}
