package com.liam.compose.core.networking.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("code")
    val code: Int? = null
)