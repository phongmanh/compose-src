package com.liam.compose.core.components.list

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException

/** Default page size when a caller does not specify one. */
const val DEFAULT_PAGE_SIZE = 20

/**
 * One page returned by a page/limit backend.
 *
 * [totalCount] is optional side-band metadata — the running total the backend reports (the "ichi"
 * endpoints send it as `recordsTotal`). Paging itself does not need it; [PageKeyedPagingSource]
 * forwards it through `onTotalCount` so a screen can show "N results" without a second request.
 */
data class Page<T : Any>(
    val items: List<T>,
    val totalCount: Int? = null,
)

/**
 * A [PagingSource] over a 1-based page/limit endpoint — the shape every "ichi" list endpoint uses,
 * and the reusable core of the endless list.
 *
 * Forward-only: it never prepends, and [getRefreshKey] always returns [firstPage]. A refresh (pull,
 * or an invalidation after the query changed) therefore reloads from the top rather than trying to
 * anchor on a scroll position the new query may not contain.
 *
 * [pageLoader] must **throw** on failure (e.g. `repository.get(...).getOrThrow()`); the thrown error
 * surfaces as [LoadResult.Error], which is what drives [PagedList]'s retryable error UI.
 */
class PageKeyedPagingSource<T : Any>(
    private val firstPage: Int = 1,
    private val onTotalCount: ((Int) -> Unit)? = null,
    private val pageLoader: suspend (page: Int, limit: Int) -> Page<T>,
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: firstPage
        return try {
            val result = pageLoader(page, params.loadSize)
            result.totalCount?.let { onTotalCount?.invoke(it) }

            // A short page (fewer rows than requested) or one that reaches the reported total is the
            // last page — stop paging so the footer spinner does not chase an empty append forever.
            // This relies on every page being a full loadSize, which pagerOf guarantees by pinning
            // initialLoadSize to pageSize.
            val reachedTotal = result.totalCount?.let { page * params.loadSize >= it } ?: false
            val isLastPage = result.items.size < params.loadSize || reachedTotal

            LoadResult.Page(
                data = result.items,
                prevKey = null, // forward-only: never prepend
                nextKey = if (result.items.isEmpty() || isLastPage) null else page + 1,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // Forward-only list: always reload from the first page after an invalidation.
    override fun getRefreshKey(state: PagingState<Int, T>): Int = firstPage
}

/**
 * Builds a [Pager] over [pageLoader], configured for an endless forward-only list.
 *
 * `initialLoadSize` is pinned to [pageSize] on purpose: with a page-keyed source the page index and
 * the load size must stay in lock-step, and Paging's default of `pageSize * 3` for the first load
 * would desync them (page 2 would skip only `pageSize` rows after a triple-size page 1). Placeholders
 * are off — the list appends, it does not present a pre-sized window of nulls.
 *
 * Collect `.flow` from the returned pager, and remember to `cachedIn(scope)` in the ViewModel.
 */
fun <T : Any> pagerOf(
    pageSize: Int = DEFAULT_PAGE_SIZE,
    firstPage: Int = 1,
    onTotalCount: ((Int) -> Unit)? = null,
    pageLoader: suspend (page: Int, limit: Int) -> Page<T>,
): Pager<Int, T> = Pager(
    config = PagingConfig(
        pageSize = pageSize,
        initialLoadSize = pageSize,
        enablePlaceholders = false,
    ),
    pagingSourceFactory = {
        PageKeyedPagingSource(
            firstPage = firstPage,
            onTotalCount = onTotalCount,
            pageLoader = pageLoader,
        )
    },
)
