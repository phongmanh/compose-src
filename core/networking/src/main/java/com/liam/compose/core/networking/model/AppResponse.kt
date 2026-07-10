package com.liam.compose.core.networking.model

import com.google.gson.annotations.SerializedName

data class AppResponse<T>(
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: Message,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("errors")
    val errors: List<ErrorInfo>? = null
)

data class Message(
    val message: String? = null,
    val exMessage: String? = null
)

data class ErrorInfo(
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("field")
    val field: String? = null
)
