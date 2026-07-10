package com.liam.compose.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liam.compose.core.datastore.UserPreferencesRepository
import com.liam.compose.features.auth.data.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /** The signed-in user, used to greet and to seed the change-password screen. */
    val user: StateFlow<UserModel> = userPreferencesRepository.userModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserModel())

    /**
     * Clears the user session (keeps the gateway token). [MainViewModel] observes the user flow and
     * returns the app to the Login screen once the user is gone.
     */
    fun logout() {
        viewModelScope.launch { userPreferencesRepository.clearUser() }
    }
}
