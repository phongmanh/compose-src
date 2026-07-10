package com.liam.compose.core.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/**
 * Reads/writes the [UserPreferences] proto to disk for the Proto DataStore.
 */
object UserPreferencesSerializer : Serializer<UserPreferences> {

    override val defaultValue: UserPreferences = UserPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserPreferences =
        try {
            UserPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read UserPreferences proto.", exception)
        }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) = t.writeTo(output)
}
