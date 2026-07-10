package com.liam.compose.features.auth.ui.state

import com.liam.compose.features.auth.data.model.UserModel

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: UserModel? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
