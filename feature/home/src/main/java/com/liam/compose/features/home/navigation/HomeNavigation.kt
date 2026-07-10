package com.liam.compose.features.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.liam.compose.features.home.ui.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeKey : NavKey {
    @Serializable
    data object Root : HomeKey
}

fun EntryProviderScope<NavKey>.homeEntries(backStack: NavBackStack<NavKey>) {
    entry(HomeKey.Root) {
        HomeScreen()
    }
}