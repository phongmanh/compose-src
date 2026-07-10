package com.liam.compose.core.networking.repository

interface ITokenProvider {
    fun accessToken(): String
}