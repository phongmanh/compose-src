package com.liam.compose.features.auth.ui.state

sealed class GatewayAuthState {
    data object Loading : GatewayAuthState()
    data object Success : GatewayAuthState()
    data object Unauthorized : GatewayAuthState()
    data class Error(val message: String) : GatewayAuthState()
}
