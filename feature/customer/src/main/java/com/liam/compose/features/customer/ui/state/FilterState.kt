package com.liam.compose.features.customer.ui.state

import com.liam.compose.core.model.UserModel
import com.liam.compose.features.customer.data.model.OptionModel
import com.liam.compose.features.customer.data.model.SearchRequest
import com.liam.compose.features.customer.util.todayApiDate

/**
 * Visit status the list can be narrowed by. [ALL] carries an empty [apiValue] because that is how
 * the backend spells "no status filter" — see [SearchRequest] on why blanks are sent, not omitted.
 */
enum class VisitStatusFilter(val apiValue: String) {
    ALL(""),
    NOT_YET("NotYet"),
    VISITED("Visited"),
}

/**
 * Everything that shapes the customer query. Owned by the ViewModel — the filter UI is stateless and
 * reports changes back up, so a rotation or process death cannot desync the visible chips from the
 * query that produced the list.
 *
 * [role] is the odd one out: it is the signed-in user's role rather than something the user picks,
 * but it travels with every request, so it lives here to keep [toSearchRequest] a pure function of
 * this state.
 */
data class FilterState(
    val keyword: String = "",
    val seller: OptionModel? = null,
    val route: OptionModel? = null,
    val status: VisitStatusFilter = VisitStatusFilter.ALL,
    val dateVisit: String? = null,
    val role: String = "",
) {
    /** Number of chip filters in effect. Excludes [keyword] and [role], which are not chips. */
    val activeFilterCount: Int
        get() = listOfNotNull(seller, route, dateVisit).size +
            if (status != VisitStatusFilter.ALL) 1 else 0

    val hasActiveFilters: Boolean
        get() = activeFilterCount > 0

    /**
     * Drops every chip filter but keeps the keyword, matching what the "clear filters" chip does.
     * [role] survives too — dropping it would send an unqualified request, which is not what the
     * user asked for by clearing the chips.
     */
    fun clearFilters(): FilterState = FilterState(keyword = keyword, role = role)

    fun toSearchRequest(page: Int, limit: Int): SearchRequest = SearchRequest(
        loginName = seller?.value.orEmpty(),
        role = role,
        dateVisit = dateVisit.orEmpty(),
        routeCode = route?.value.orEmpty(),
        status = status.apiValue,
        keyword = keyword.trim(),
        page = page,
        limit = limit,
    )

    /** Log-safe summary — codes only, never customer-identifying keyword text. */
    fun logSummary(): String =
        "seller=${seller?.value}, role=$role, route=${route?.value}, status=${status.apiValue}, " +
            "date=$dateVisit, keywordLength=${keyword.trim().length}"

    companion object {
        /**
         * What the list opens with: the signed-in user's own outstanding visits for today. Built
         * from [user] rather than being a constructor default because both the seller and the date
         * are only knowable at runtime.
         */
        fun defaultFor(user: UserModel, today: String = todayApiDate()): FilterState = FilterState(
            seller = user.userName?.let { OptionModel(value = it, text = user.fullName ?: it) },
            status = VisitStatusFilter.NOT_YET,
            dateVisit = today,
            role = user.role.orEmpty(),
        )
    }
}
