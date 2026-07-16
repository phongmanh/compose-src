package com.liam.compose.features.auth.ui.state

import com.liam.compose.core.model.UserModel

data class LoginUiState(
    val isLoading: Boolean = false,
    val user: UserModel? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
