package com.liam.compose.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.liam.compose.core.datastore.UserPreferences
import com.liam.compose.core.datastore.UserPreferencesSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<UserPreferences> = DataStoreFactory.create(
        serializer = UserPreferencesSerializer,
        // No legacy data to migrate today. This is the extension point for a future
        // breaking change to the UserPreferences schema: add a DataMigration<UserPreferences>
        // here when one is actually needed.
        migrations = emptyList<DataMigration<UserPreferences>>(),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.dataStoreFile("user_prefs.pb") }
    )
}
