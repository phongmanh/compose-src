package com.liam.compose.features.customer.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.liam.compose.core.components.list.PagedList
import com.liam.compose.core.networking.remote.ApiException
import com.liam.compose.features.customer.R
import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.navigation.NEW_CUSTOMER_ID
import com.liam.compose.features.customer.ui.screens.components.CardCorner
import com.liam.compose.features.customer.ui.screens.components.CustomerListRow
import com.liam.compose.features.customer.ui.screens.components.CustomerListSkeleton
import com.liam.compose.features.customer.ui.screens.components.CustomerSearchField
import com.liam.compose.features.customer.ui.screens.components.FilterBar
import com.liam.compose.features.customer.ui.screens.components.GradientHeader
import com.liam.compose.features.customer.ui.screens.components.HeaderContent
import com.liam.compose.features.customer.ui.screens.components.HeaderContentMuted
import com.liam.compose.features.customer.ui.screens.components.IconChip
import com.liam.compose.features.customer.ui.screens.components.ScreenPadding
import com.liam.compose.features.customer.ui.state.CustomerListUiState
import com.liam.compose.features.customer.ui.state.FilterState
import com.liam.compose.features.customer.ui.viewmodel.CustomerListViewModel
import kotlinx.coroutines.launch

/**
 * Customer list with search, filtering and paging.
 *
 * The list itself is the shared [PagedList] (endless scroll, pull-to-refresh, load/empty/error/append
 * states); this screen supplies the customer-branded slots and keeps the filter section mounted across
 * every list state so the user can always correct the query that produced an empty or failed result.
 */
@Composable
fun CustomerListScreen(
    onNavigateToForm: (customerId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val customers = viewModel.pagedCustomers.collectAsLazyPagingItems()

    CustomerListScreen(
        uiState = uiState,
        totalCount = totalCount,
        customers = customers,
        onFilterChange = viewModel::onFilterChange,
        onKeywordChange = viewModel::onKeywordChange,
        onCustomerVisited = viewModel::onCustomerVisited,
        onNavigateToForm = onNavigateToForm,
        modifier = modifier,
    )
}

/** Stateless body — renders the paging state and forwards events, so it stays previewable. */
@Composable
private fun CustomerListScreen(
    uiState: CustomerListUiState,
    totalCount: Int?,
    customers: LazyPagingItems<CustomerModel>,
    onFilterChange: (FilterState) -> Unit,
    onKeywordChange: (String) -> Unit,
    onCustomerVisited: (Int) -> Unit,
    onNavigateToForm: (customerId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Hoisted here so both the list and the scroll-to-top action read the same scroll position.
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // derivedStateOf keeps the position read off the recomposition hot path: the flag only flips when
    // the first item scrolls off (or back on), not on every scrolled pixel.
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    // Log once per flip of the flag, not per recomposition. A SideEffect inside the
    // AnimatedVisibility content fired on every enter/exit animation frame and parent
    // recomposition (and even logged `false` while the exit animation was still running).
    LaunchedEffect(showScrollToTop) {
        Log.i(
            "CustomerListScreen",
            "showScrollToTop=$showScrollToTop, firstVisibleItemIndex=${listState.firstVisibleItemIndex}",
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // The header paints its own status-bar inset so the gradient can run full-bleed; the host
        // Scaffold in :app already handles the bottom and horizontal ones.
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Only offered once the first item has scrolled off, so a short, unscrolled list
                // never shows it. Quieter secondary colours keep "create" the primary action.
                AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    SmallFloatingActionButton(
                        onClick = { scope.launch { listState.animateScrollToItem(0) } },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.customer_action_scroll_top),
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { onNavigateToForm(NEW_CUSTOMER_ID) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.customer_button_create)) },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // onKeywordChange comes straight from the ViewModel rather than being rebuilt from
            // uiState here — a lambda capturing uiState changes identity on every emission and would
            // recompose the header on updates that touch neither the count nor the keyword.
            CustomerListHeader(
                totalCount = totalCount,
                keyword = uiState.filter.keyword,
                onKeywordChange = onKeywordChange,
            )

            FilterBar(
                filter = uiState.filter,
                sellers = uiState.sellers,
                routes = uiState.routes,
                onFilterChange = onFilterChange,
            )

            PagedList(
                items = customers,
                listState = listState,
                modifier = Modifier.fillMaxSize(),
                // Bottom padding clears the extended FAB so the last card is never trapped under it.
                contentPadding = PaddingValues(
                    start = ScreenPadding,
                    end = ScreenPadding,
                    top = 4.dp,
                    bottom = ListBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                // Paging carries only the Throwable; map it to the user-facing message the API set.
                errorMessage = { (it as? ApiException)?.userMessage ?: it.message.orEmpty() },
                loading = { CustomerListSkeleton() },
                empty = {
                    EmptyContent(
                        hasActiveFilters = uiState.filter.hasActiveFilters,
                        onClearFilters = { onFilterChange(uiState.filter.clearFilters()) },
                    )
                },
                error = { message, onRetry -> ErrorContent(message = message, onRetry = onRetry) },
            ) { customer ->
                CustomerListRow(
                    customer = customer,
                    onEdit = { customer.customerId?.let(onNavigateToForm) },
                    onVisited = { customer.customerId?.let(onCustomerVisited) },
                )
            }
        }
    }
}

/**
 * Gold header carrying the screen title, the result count and the search field.
 *
 * The count lives here rather than in a list header row so it stays visible while the list area is
 * loading or scrolled; it is null whenever there is no successful result to count.
 */
@Composable
private fun CustomerListHeader(
    totalCount: Int?,
    keyword: String,
    onKeywordChange: (String) -> Unit,
) {
    GradientHeader {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = null,
                    tint = HeaderContent,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.customer_list_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HeaderContent,
                )
                if (totalCount != null) {
                    Text(
                        text = stringResource(R.string.customer_list_count, totalCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = HeaderContentMuted,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        CustomerSearchField(keyword = keyword, onKeywordChange = onKeywordChange)
    }
}

@Composable
private fun EmptyContent(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
) {
    MessageContent(
        icon = Icons.Outlined.SearchOff,
        accent = MaterialTheme.colorScheme.primary,
        title = stringResource(R.string.customer_empty_title),
        message = stringResource(R.string.customer_empty_message),
    ) {
        if (hasActiveFilters) {
            Button(onClick = onClearFilters, shape = RoundedCornerShape(CardCorner)) {
                Text(stringResource(R.string.customer_empty_clear_filters))
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    MessageContent(
        icon = Icons.Outlined.ErrorOutline,
        accent = MaterialTheme.colorScheme.error,
        title = stringResource(R.string.customer_error_title),
        message = message.ifBlank { stringResource(R.string.customer_error_generic) },
    ) {
        Button(onClick = onRetry, shape = RoundedCornerShape(CardCorner)) {
            Text(stringResource(R.string.customer_action_retry))
        }
    }
}

/**
 * Shared centred message layout. Scrollable so pull-to-refresh still works when the list area holds
 * no scrollable content of its own.
 */
@Composable
private fun MessageContent(
    icon: ImageVector,
    accent: Color,
    title: String,
    message: String,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconChip(icon = icon, accent = accent, size = 72.dp, iconSize = 36.dp)
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
        )
        action()
    }
}

// Internal so CustomerListSkeleton can pad on the same grid as the real list.
internal val ListBottomPadding = 96.dp
