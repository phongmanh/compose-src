package com.liam.compose.features.customer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseWrap<T>(
    val recordsTotal: Int? = null,
    val recordsFiltered: Int? = null,
    val data: List<T>? = null,
)
