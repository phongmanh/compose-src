package com.liam.compose.features.customer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.liam.compose.core.navigation.LocalNavBackStack
import com.liam.compose.core.navigation.navigate
import com.liam.compose.core.navigation.pop
import com.liam.compose.features.customer.ui.screens.CustomerFormScreen
import com.liam.compose.features.customer.ui.screens.CustomerListScreen
import kotlinx.serialization.Serializable

/** Sentinel [CustomerKey.Form.customerId] meaning "create", as opposed to editing an existing id. */
const val NEW_CUSTOMER_ID = 0

@Serializable
sealed interface CustomerKey : NavKey {
    @Serializable
    data object Root : CustomerKey

    @Serializable
    data class Form(val customerId: Int = NEW_CUSTOMER_ID) : CustomerKey
}

fun EntryProviderScope<NavKey>.customerEntries(backStack: NavBackStack<NavKey>) {
    entry<CustomerKey.Root> {
        CustomerListScreen(
            onNavigateToForm = { customerId -> backStack.navigate(CustomerKey.Form(customerId)) },
        )
    }
    entry<CustomerKey.Form> { key ->
        val navBackStack = LocalNavBackStack.current
        CustomerFormScreen(
            customerId = key.customerId,
            onSuccess = { navBackStack.pop() },
            onBack = { navBackStack.pop() },
        )
    }
}
