package com.liam.compose.features.customer.ui.state

import com.liam.compose.features.customer.data.model.OptionModel

/**
 * The parts of the customer list screen that live *outside* the paged list itself.
 *
 * The list content — loading, empty, error, the rows, and the append footer — is driven by Paging 3's
 * [androidx.paging.compose.LazyPagingItems] load state, and the header count by the ViewModel's
 * `totalCount`. What remains here is the filter section, which has to stay on screen and interactive
 * across every list state so the user can always correct the query that produced an empty or failed
 * result. A flat sealed hierarchy would hide the filters exactly when they are needed.
 */
data class CustomerListUiState(
    val filter: FilterState = FilterState(),
    val sellers: List<OptionModel> = emptyList(),
    val routes: List<OptionModel> = emptyList(),
)
