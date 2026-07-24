package com.liam.compose.features.customer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.liam.compose.core.components.list.Page
import com.liam.compose.core.components.list.pagerOf
import com.liam.compose.core.datastore.UserPreferencesRepository
import com.liam.compose.core.model.UserModel
import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.data.repository.CustomerRepository
import com.liam.compose.features.customer.ui.state.CustomerListUiState
import com.liam.compose.features.customer.ui.state.FilterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val repository: CustomerRepository,
    private val userPref: UserPreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerListUiState())
    val uiState: StateFlow<CustomerListUiState> = _uiState.asStateFlow()

    /**
     * Total customers the current query reported, for the header count. Sourced from the response
     * envelope's `recordsTotal` via [pagerFor] rather than from the loaded page count, and reset to
     * null for each new query so the header does not show a stale total while the next list loads.
     */
    private val _totalCount = MutableStateFlow<Int?>(null)
    val totalCount: StateFlow<Int?> = _totalCount.asStateFlow()

    /**
     * Every filter mutation lands here. Starts null so the paging pipeline stays idle until
     * [applyDefaultFilter] has read the signed-in user — paging before then would fire a request with
     * no seller or role.
     */
    private val filterChanges = MutableStateFlow<FilterState?>(null)

    /**
     * The customer pages for the current filter, ready to be collected as `LazyPagingItems`.
     *
     * Keyword edits are debounced so typing does not spin up a new PagingSource per keystroke, while
     * chip and date changes apply immediately; combining the two streams means a chip tap reloads
     * right away using whatever keyword has settled so far, and clearing the keyword skips the debounce
     * (going back to "no search" should feel instant). A newer filter cancels the previous pager via
     * [flatMapLatest] and [cachedIn] keeps the loaded pages across configuration changes, so pages
     * from a stale query can never be stitched onto the new list.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pagedCustomers: Flow<PagingData<CustomerModel>> = run {
        val filters = filterChanges.filterNotNull()

        val keywords = filters
            .map { it.keyword.trim() }
            .distinctUntilChanged()
            .debounce { keyword -> if (keyword.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }

        val otherFilters = filters
            .map { it.copy(keyword = "") }
            .distinctUntilChanged()

        combine(keywords, otherFilters) { keyword, filter -> filter.copy(keyword = keyword) }
            .distinctUntilChanged()
            .flatMapLatest { filter -> pagerFor(filter) }
            .cachedIn(viewModelScope)
    }

    init {
        Log.d(TAG, "init: applying default filter and loading filter options")
        viewModelScope.launch {
            val user = userPref.userModel.first()
            applyDefaultFilter(user)
            loadFilterOptions(user)
        }
    }

    // region Events from the UI

    fun onFilterChange(filter: FilterState) {
        Log.d(TAG, "onFilterChange: ${filter.logSummary()}")
        _uiState.update { it.copy(filter = filter) }
        filterChanges.value = filter
    }

    /**
     * Keyword edits fold into the current filter here rather than in the composable, so the search
     * field can be handed a callback whose identity does not change with every ui state emission.
     */
    fun onKeywordChange(keyword: String) {
        onFilterChange(_uiState.value.filter.copy(keyword = keyword))
    }

    fun onCustomerVisited(customerId: Int) {
        // Stub for now - check-in wizard is out of scope.
        Log.d(TAG, "onCustomerVisited: check-in not yet implemented for customerId=$customerId")
    }

    // endregion

    /**
     * Builds the page flow for [filter]. Resets the header total up front and lets the reusable
     * [pagerOf] source report the new one through `onTotalCount`. The page loader throws on failure
     * (`getOrThrow`) so a load error surfaces as Paging's `LoadState.Error` and drives the retry UI.
     */
    private fun pagerFor(filter: FilterState): Flow<PagingData<CustomerModel>> {
        Log.d(TAG, "pagerFor: new pager for ${filter.logSummary()}")
        _totalCount.value = null
        return pagerOf(
            pageSize = PAGE_SIZE,
            onTotalCount = { total -> _totalCount.value = total },
        ) { page, limit ->
            val response = repository
                .getCustomers(filter.toSearchRequest(page = page, limit = limit))
                .getOrThrow()
            Page(
                items = response.data?.items.orEmpty(),
                totalCount = response.data?.totalCount,
            )
        }.flow
    }

    /**
     * Seeds the filter with the signed-in user's own outstanding visits for today and releases the
     * paging pipeline. The seller chip is pre-selected rather than left blank so the first request
     * carries a `loginName`.
     */
    private fun applyDefaultFilter(user: UserModel) {
        val filter = FilterState.defaultFor(user)
        Log.d(TAG, "applyDefaultFilter: ${filter.logSummary()}")
        _uiState.update { it.copy(filter = filter) }
        filterChanges.value = filter
    }

    private suspend fun loadFilterOptions(user: UserModel) {
        Log.d(TAG, "loadFilterOptions: enter")
        val userName = user.userName.orEmpty()
        val userRole = user.role.orEmpty()

        val sellers = repository.getSalesByRole(userRole, userName)
            .onFailure { Log.w(TAG, "loadFilterOptions: seller options failed", it) }
            .getOrNull()?.data.orEmpty()
        val routes = repository.getRoutesByUser(userName)
            .onFailure { Log.w(TAG, "loadFilterOptions: route options failed", it) }
            .getOrNull()?.data.orEmpty()

        _uiState.update { it.copy(sellers = sellers, routes = routes) }
        Log.d(TAG, "loadFilterOptions: exit, sellers=${sellers.size}, routes=${routes.size}")
    }

    private companion object {
        const val TAG = "CustomerListVM"
        const val PAGE_SIZE = 10
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
