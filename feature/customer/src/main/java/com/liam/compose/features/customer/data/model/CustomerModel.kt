package com.liam.compose.features.customer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomerModel(
    val id: Int? = null,
    val customerId: Int? = null,
    val customerCode: String? = null,
    val customerName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val customerTypeName: String? = null,
    val visitStatus: String? = null,
)
