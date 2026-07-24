package com.liam.compose.features.customer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class OptionModel(
    val value: String,
    val text: String,
)
