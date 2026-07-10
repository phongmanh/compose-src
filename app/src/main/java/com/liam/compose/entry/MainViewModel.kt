package com.liam.compose.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liam.compose.core.datastore.UserPreferencesRepository
import com.liam.compose.features.auth.gateway.GatewayTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPref: UserPreferencesRepository,
    private val gatewayTokenManager: GatewayTokenManager
) : ViewModel() {

    private var gatewayState = MutableStateFlow<GatewayAuthState>(GatewayAuthState.Unauthenticated)
    val gatewayAuthState: StateFlow<GatewayAuthState> = gatewayState

    val uiState: StateFlow<AuthState> = userPref.userModel.map { userModel ->
        if (userModel.userName != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)

    init {
        viewModelScope.launch {
            // Ensure gateway token is available
            if (userPref.gatewayAuthModel.first().accessToken == null) {
                val result = gatewayTokenManager.ensureGatewayToken()
                if (result.isFailure) {
                    // Handle failure to get gateway token if needed
                    gatewayState.value = GatewayAuthState.Failed
                } else {
                    gatewayState.value = GatewayAuthState.Authenticated
                }
            } else {
                gatewayState.value = GatewayAuthState.Authenticated
            }

        }
    }

    /** Returns true if the app should show the first screen. */
    fun shouldShowFirstScreen(): Boolean = gatewayState.value !is GatewayAuthState.Authenticated

}
