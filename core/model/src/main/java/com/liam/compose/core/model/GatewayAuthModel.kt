package com.liam.compose.core.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class GatewayAuthModel(
    @SerializedName("accessToken")
    val accessToken: String? = null,
    @SerializedName("accessTokenLifeTime")
    val accessTokenLifeTime: Int? = null,
    @SerializedName("accessTokenExpireDate")
    val accessTokenExpireDate: Date? = null
)
