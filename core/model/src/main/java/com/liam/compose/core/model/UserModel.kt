package com.liam.compose.core.model

import com.google.gson.annotations.SerializedName

data class UserModel(
    @SerializedName("userName")
    val userName: String? = null,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("role")
    val role: String? = null
)
