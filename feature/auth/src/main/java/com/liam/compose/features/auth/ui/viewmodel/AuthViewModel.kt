package com.liam.compose.features.auth.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liam.compose.features.auth.data.model.AuthPostRequest
import com.liam.compose.features.auth.data.model.ChangePassModel
import com.liam.compose.core.model.UserModel
import com.liam.compose.features.auth.data.repository.AuthRepository
import com.liam.compose.core.model.IAuthSessionStore
import com.liam.compose.features.auth.ui.state.GatewayAuthState
import com.liam.compose.features.auth.ui.state.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val iAuthSessionStore: IAuthSessionStore
) : ViewModel() {


    /**
     * Login state
     */
    private val _loginUiState = mutableStateOf(LoginUiState())
    val loginUiState: State<LoginUiState> = _loginUiState

    /**
     * Current user
     */
    private var _currentUser: UserModel? = null
    val currentUser: UserModel?
        get() = _currentUser

    /**
     * Login user
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(isLoading = true)

            authRepository.auth(AuthPostRequest(username, password))
                .onSuccess { data ->
                    _currentUser = data.data
                    _currentUser?.let { iAuthSessionStore.saveUser(it) }
                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        user = _currentUser,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    // ApiException (from ErrorMapper) always carries a user-facing message.
                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message,
                        isSuccess = false
                    )
                }
        }
    }

    fun logout() {
        _currentUser = null
        _loginUiState.value = LoginUiState()
        viewModelScope.launch { iAuthSessionStore.clear() }
    }

    /**
     * Clears the transient login result. This ViewModel is scoped to the auth NavEntry, which the
     * viewmodel-store decorator keeps alive (Activity-scoped) across the login -> app -> logout
     * round trip. Without resetting, the `isSuccess = true` from a previous login would linger and
     * make [com.liam.compose.features.auth.ui.screens.LoginScreen] show a spinner instead
     * of the form when the user returns to Login after logging out.
     */
    fun resetLoginState() {
        _currentUser = null
        _loginUiState.value = LoginUiState()
    }

    fun changePassword(username: String, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _loginUiState.value = _loginUiState.value.copy(isLoading = true)

            val changePassModel = ChangePassModel(
                userName = username,
                oldPassword = oldPassword,
                newPassword = newPassword
            )

            authRepository.changePassword(changePassModel)
                .onSuccess {
                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->
                    _loginUiState.value = _loginUiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message,
                        isSuccess = false
                    )
                }
        }
    }
}
