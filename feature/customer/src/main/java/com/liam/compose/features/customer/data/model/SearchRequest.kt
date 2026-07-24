package com.liam.compose.features.customer.data.model

import kotlinx.serialization.Serializable

/**
 * Body of the customer-visit search.
 *
 * Every field is a non-null [String] defaulting to `""` rather than a nullable one: Gson omits null
 * fields from the serialized body, and the backend expects the keys to be present with an empty
 * value to mean "no filter".
 */
@Serializable
data class SearchRequest(
    val loginName: String = "",
    val role: String = "",
    val dateVisit: String = "",
    val routeCode: String = "",
    val status: String = "",
    val keyword: String = "",
    val page: Int = 1,
    val limit: Int = 10,
)
