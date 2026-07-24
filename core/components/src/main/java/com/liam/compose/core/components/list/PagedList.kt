package com.liam.compose.core.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.liam.compose.core.components.R

/**
 * A reusable endless list over Paging 3.
 *
 * It owns every state an infinite list goes through so a feature never re-implements them:
 * - first-page load ([loading]) vs. a pull-to-refresh over existing items (the pull spinner);
 * - a first-page failure with nothing on screen ([error], retryable);
 * - a query that matched nothing ([empty]);
 * - endless append as the user scrolls, with its own footer spinner ([appendLoading]) and a footer
 *   retry when appending a page fails ([appendError]) — a failure there keeps the loaded items;
 * - pull-to-refresh (toggle with [enablePullToRefresh]).
 *
 * The list content, header/FAB and any filter chrome stay with the feature; this composable is only
 * the scrollable list area. Every state has a sensible default and an override slot, so a plain list
 * needs just [items] and [itemContent], while a branded one (e.g. the customer list) swaps in its own
 * skeleton, empty and error screens.
 *
 * Feed it from [pagerOf]; map load errors to human text with [errorMessage] (Paging only carries the
 * [Throwable]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> PagedList(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    enablePullToRefresh: Boolean = true,
    itemKey: ((item: T) -> Any)? = null,
    errorMessage: (Throwable) -> String = { it.localizedMessage.orEmpty() },
    loading: @Composable () -> Unit = { PagedListLoading() },
    empty: @Composable () -> Unit = { PagedListEmpty() },
    error: @Composable (message: String, onRetry: () -> Unit) -> Unit = { message, onRetry ->
        PagedListError(message = message, onRetry = onRetry)
    },
    appendLoading: @Composable () -> Unit = { PagedListAppendLoading() },
    appendError: @Composable (message: String, onRetry: () -> Unit) -> Unit = { message, onRetry ->
        PagedListAppendError(message = message, onRetry = onRetry)
    },
    itemContent: @Composable (item: T) -> Unit,
) {
    val refresh = items.loadState.refresh

    val body: @Composable () -> Unit = {
        when {
            // First page in flight with nothing to show yet.
            refresh is LoadState.Loading && items.itemCount == 0 -> loading()

            // First page failed and there is nothing on screen to fall back to.
            refresh is LoadState.Error && items.itemCount == 0 ->
                error(errorMessage(refresh.error), items::retry)

            // Loaded successfully, but the query matched nothing. endOfPaginationReached guards
            // against the brief pre-load frame where refresh is NotLoading but no page has arrived.
            refresh is LoadState.NotLoading &&
                items.itemCount == 0 &&
                items.loadState.append.endOfPaginationReached -> empty()

            else -> PagedListContent(
                items = items,
                listState = listState,
                contentPadding = contentPadding,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                itemKey = itemKey,
                errorMessage = errorMessage,
                appendLoading = appendLoading,
                appendError = appendError,
                itemContent = itemContent,
            )
        }
    }

    if (enablePullToRefresh) {
        // itemCount > 0 distinguishes a user-initiated refresh (keep the list, show the pull spinner)
        // from the very first load (show the loading slot, no spinner).
        val isRefreshing = refresh is LoadState.Loading && items.itemCount > 0
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = items::refresh,
            modifier = modifier.fillMaxSize(),
        ) {
            body()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) { body() }
    }
}

@Composable
private fun <T : Any> PagedListContent(
    items: LazyPagingItems<T>,
    listState: LazyListState,
    contentPadding: PaddingValues,
    verticalArrangement: Arrangement.Vertical,
    horizontalAlignment: Alignment.Horizontal,
    itemKey: ((item: T) -> Any)?,
    errorMessage: (Throwable) -> String,
    appendLoading: @Composable () -> Unit,
    appendError: @Composable (message: String, onRetry: () -> Unit) -> Unit,
    itemContent: @Composable (item: T) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        // Paging drives the endless append itself: reading items[index] near the end triggers the
        // next page, so there is no manual scroll-position threshold to maintain here.
        items(
            count = items.itemCount,
            key = items.itemKey(itemKey),
        ) { index ->
            items[index]?.let { itemContent(it) }
        }

        when (val append = items.loadState.append) {
            is LoadState.Loading -> item(key = APPEND_FOOTER_KEY) { appendLoading() }
            is LoadState.Error -> item(key = APPEND_FOOTER_KEY) {
                appendError(errorMessage(append.error), items::retry)
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

/** Centred spinner for the first-page load. Override [PagedList]'s `loading` slot for a skeleton. */
@Composable
private fun PagedListLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PagedListEmpty(modifier: Modifier = Modifier) {
    PagedListMessage(
        message = stringResource(R.string.paged_list_empty),
        modifier = modifier,
    )
}

@Composable
private fun PagedListError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PagedListMessage(
        message = message.ifBlank { stringResource(R.string.paged_list_error) },
        modifier = modifier,
    ) {
        Button(onClick = onRetry) { Text(stringResource(R.string.paged_list_retry)) }
    }
}

/**
 * Centred message with an optional action. Scrollable so pull-to-refresh keeps working even when the
 * list area holds no scrollable content of its own (empty/error states).
 */
@Composable
private fun PagedListMessage(
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/** Footer spinner shown while the next page is appending. */
@Composable
private fun PagedListAppendLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
    }
}

/** Footer retry shown when appending a page failed — the already-loaded items stay on screen. */
@Composable
private fun PagedListAppendError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message.ifBlank { stringResource(R.string.paged_list_error) },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onRetry) { Text(stringResource(R.string.paged_list_load_more)) }
    }
}

private const val APPEND_FOOTER_KEY = "paged_list_append_footer"
