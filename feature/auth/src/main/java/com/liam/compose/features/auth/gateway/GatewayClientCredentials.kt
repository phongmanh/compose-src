package com.liam.compose.features.auth.gateway

import com.liam.compose.features.auth.BuildConfig

/**
 * Client (gateway) credentials used to obtain the app-level gateway token before any user logs in.
 * Kept in one place so the splash bootstrap ([com.liam.compose.features.auth.gateway.GatewayTokenManager])
 * and [com.liam.compose.features.auth.ui.viewmodel.AuthViewModel] don't duplicate them.
 * The values come from BuildConfig, injected at build time from git-ignored secrets.properties
 * (Guideline §9 — no secrets in source/version control).
 */
object GatewayClientCredentials {
    val CLIENT_ID: String = BuildConfig.GATEWAY_CLIENT_ID
    val CLIENT_SECRET: String = BuildConfig.GATEWAY_CLIENT_SECRET
}
