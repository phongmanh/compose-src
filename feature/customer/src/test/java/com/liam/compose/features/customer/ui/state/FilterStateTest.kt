package com.liam.compose.features.customer.ui.state

import com.liam.compose.core.model.UserModel
import com.liam.compose.features.customer.data.model.OptionModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterStateTest {

    @Test
    fun `default state has no active filters`() {
        val filter = FilterState()

        assertEquals(0, filter.activeFilterCount)
        assertFalse(filter.hasActiveFilters)
    }

    @Test
    fun `keyword alone is not counted as an active filter`() {
        val filter = FilterState(keyword = "an")

        assertFalse(filter.hasActiveFilters)
    }

    @Test
    fun `each chip filter adds to the active count`() {
        val filter = FilterState(
            seller = SELLER,
            route = ROUTE,
            status = VisitStatusFilter.VISITED,
            dateVisit = "2026-07-20",
        )

        assertEquals(4, filter.activeFilterCount)
        assertTrue(filter.hasActiveFilters)
    }

    @Test
    fun `status ALL is not counted as an active filter`() {
        val filter = FilterState(status = VisitStatusFilter.ALL)

        assertEquals(0, filter.activeFilterCount)
    }

    @Test
    fun `role is not counted as an active filter`() {
        val filter = FilterState(role = "Admin")

        assertEquals(0, filter.activeFilterCount)
    }

    @Test
    fun `clearFilters drops chips but keeps the keyword and role`() {
        val filter = FilterState(
            keyword = "an",
            seller = SELLER,
            route = ROUTE,
            status = VisitStatusFilter.NOT_YET,
            dateVisit = "2026-07-20",
            role = "Admin",
        )

        val cleared = filter.clearFilters()

        assertEquals("an", cleared.keyword)
        assertEquals("Admin", cleared.role)
        assertFalse(cleared.hasActiveFilters)
    }

    @Test
    fun `toSearchRequest maps selected options to their api values`() {
        val filter = FilterState(
            keyword = "an",
            seller = SELLER,
            route = ROUTE,
            status = VisitStatusFilter.VISITED,
            dateVisit = "2026-07-20",
            role = "Admin",
        )

        val request = filter.toSearchRequest(page = 2, limit = 10)

        assertEquals("seller-1", request.loginName)
        assertEquals("Admin", request.role)
        assertEquals("route-1", request.routeCode)
        assertEquals("Visited", request.status)
        assertEquals("an", request.keyword)
        assertEquals("2026-07-20", request.dateVisit)
        assertEquals(2, request.page)
        assertEquals(10, request.limit)
    }

    @Test
    fun `toSearchRequest sends unset filters as blanks so the keys are not omitted`() {
        val request = FilterState().toSearchRequest(page = 1, limit = 10)

        assertEquals("", request.loginName)
        assertEquals("", request.role)
        assertEquals("", request.routeCode)
        assertEquals("", request.status)
        assertEquals("", request.keyword)
        assertEquals("", request.dateVisit)
    }

    @Test
    fun `toSearchRequest trims the keyword`() {
        val request = FilterState(keyword = "  an  ").toSearchRequest(page = 1, limit = 10)

        assertEquals("an", request.keyword)
    }

    @Test
    fun `default filter searches the signed-in user's outstanding visits for today`() {
        val filter = FilterState.defaultFor(ADMIN, today = "2026-07-20")

        val request = filter.toSearchRequest(page = 1, limit = 10)

        assertEquals("admin", request.loginName)
        assertEquals("Admin", request.role)
        assertEquals("2026-07-20", request.dateVisit)
        assertEquals("", request.routeCode)
        assertEquals("NotYet", request.status)
        assertEquals("", request.keyword)
        assertEquals(1, request.page)
        assertEquals(10, request.limit)
    }

    @Test
    fun `default filter labels the seller chip with the user's full name`() {
        val filter = FilterState.defaultFor(ADMIN, today = "2026-07-20")

        assertEquals(OptionModel(value = "admin", text = "Administrator"), filter.seller)
    }

    @Test
    fun `default filter leaves the seller unset when no user is stored`() {
        val filter = FilterState.defaultFor(UserModel(), today = "2026-07-20")

        assertEquals("", filter.toSearchRequest(page = 1, limit = 10).loginName)
    }

    @Test
    fun `logSummary reports keyword length instead of keyword text`() {
        val summary = FilterState(keyword = "Nguyen").logSummary()

        assertTrue(summary.contains("keywordLength=6"))
        assertFalse(summary.contains("Nguyen"))
    }

    private companion object {
        val SELLER = OptionModel(value = "seller-1", text = "Seller One")
        val ROUTE = OptionModel(value = "route-1", text = "Route One")
        val ADMIN = UserModel(userName = "admin", fullName = "Administrator", role = "Admin")
    }
}
