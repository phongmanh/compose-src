package com.liam.compose.features.home.ui

import com.liam.compose.core.model.UserModel

/**
 * Everything the [HomeScreen] renders in its header + summary card. Time-keeping values are
 * placeholders until the attendance ("Chấm công") API is wired up.
 */
sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val data: UserModel) : HomeUiState
    object Error : HomeUiState
}
