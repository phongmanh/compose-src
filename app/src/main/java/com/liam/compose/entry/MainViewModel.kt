package com.liam.compose.entry

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liam.compose.core.datastore.UserPreferencesRepository
import com.liam.compose.features.auth.gateway.GatewayTokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPref: UserPreferencesRepository,
    private val gatewayTokenManager: GatewayTokenManager
) : ViewModel() {

    /*
        val uiState: StateFlow<AuthState> = userPref.userModel.map { userModel ->
            if (userModel.userName != null) {
                AuthState.Authenticated
            } else {
                AuthState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthState.Loading)
    */
    private var mUIState = MutableStateFlow<AuthState>(AuthState.Loading)
    val uiState: StateFlow<AuthState> = mUIState.asStateFlow()

    // One-time event
    // We can use ShareFlow, but it can be missing event in case have no collector when the event is fired.
    private var mNotify = Channel<String>(Channel.BUFFERED)
    val notify = mNotify.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Ensure gateway token is available
            if (userPref.gatewayAuthModel.first().accessToken == null) {
                println("Gateway un-authentication.")
                val result = gatewayTokenManager.ensureGatewayToken()
                if (result.isFailure) {
                    // Handle failure to get gateway token if needed
                    mUIState.value = AuthState.Failed
                } else {
                    println("Gateway Authentication.")
                    auth()
                }
            } else {
                println("Gateway already Authentication.")
                auth()
            }

        }
    }

    // Check login status
    private suspend fun auth() {

        userPref.userModel.collect { (userName, _, _) ->
            userName?.let {
                println("User already login.")
                // LoggedIn already, navigate to Home screen
                mUIState.value = AuthState.Authenticated
            } ?: run {
                println("User does not login.")
                // Navigate to Log-in screen
                mUIState.value = AuthState.UnAuthenticated
            }
        }
    }

    // Login success
    fun onLoginSuccess() {
        println("User login success.")
        mUIState.value = AuthState.Authenticated
    }

    /** Returns true if the app should show the first screen. */
    fun shouldShowFirstScreen(): Boolean = mUIState.value == AuthState.Loading

}
