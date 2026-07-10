package com.liam.compose.features.auth.data.model

import com.google.gson.annotations.SerializedName

data class ChangePassModel(
    @SerializedName("userName")
    val userName: String,
    @SerializedName("oldPassword")
    val oldPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)
