package com.liam.compose.features.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.liam.compose.features.auth.ui.screens.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthKey : NavKey {
    @Serializable
    data object Root : AuthKey
}

fun EntryProviderScope<NavKey>.authEntries(onLoginSuccess: () -> Unit) {
    entry(AuthKey.Root) {
        LoginScreen(onLoginSuccess = onLoginSuccess)
    }
}