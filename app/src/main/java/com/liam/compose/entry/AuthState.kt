package com.liam.compose.entry

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
}

sealed class GatewayAuthState {
    object Authenticated : GatewayAuthState()
    object Unauthenticated : GatewayAuthState()
    object Failed : GatewayAuthState()
}