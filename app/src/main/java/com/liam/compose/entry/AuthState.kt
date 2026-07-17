package com.liam.compose.entry

sealed class AuthState {
    // Init state
    object Loading : AuthState()
    object Authenticated : AuthState()
    object UnAuthenticated : AuthState()
    object Failed : AuthState()
}