package com.liam.compose.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * App Settings
 */
val Context.settingDataStore by preferencesDataStore(name = "settings")
val DARK_MODE = booleanPreferencesKey("dark_mode")

suspend fun setDarkMode(context: Context, enabled: Boolean) {
    context.settingDataStore.edit { dataStore ->
        dataStore[DARK_MODE] = enabled
    }
}

fun getDarkMode(context: Context): Flow<Boolean> {
    return context.settingDataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: false
    }
}

