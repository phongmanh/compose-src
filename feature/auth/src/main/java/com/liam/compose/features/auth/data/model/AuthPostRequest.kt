package com.liam.compose.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class AuthPostRequest(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("password")
    val password: String
)
