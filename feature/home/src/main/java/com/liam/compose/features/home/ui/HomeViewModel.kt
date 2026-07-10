package com.liam.compose.features.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liam.compose.core.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    /** Header + summary-card state. The greeting name follows the persisted user session. */
    val uiState: StateFlow<HomeUiState> = userPreferencesRepository.userModel
        .map { user ->
            if (user.userName != null) {
                HomeUiState.Success(user)
            } else {
                HomeUiState.Error
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
