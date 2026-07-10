package com.liam.compose.core.datastore

import androidx.datastore.core.DataStore
import com.liam.compose.features.auth.data.model.GatewayAuthModel
import com.liam.compose.features.auth.data.model.UserModel
import com.liam.compose.features.auth.data.repository.IAuthSessionStore
import com.liam.compose.core.networking.repository.ITokenProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists [GatewayAuthModel] and [UserModel] through the [UserPreferences] Proto DataStore
 * and exposes them back as domain models.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) : IAuthSessionStore, ITokenProvider {

    val gatewayAuthModel: Flow<GatewayAuthModel> = dataStore.data.map { it.auth.toAuthModel() }
    val userModel: Flow<UserModel> = dataStore.data.map { it.user.toUserModel() }

    /** Current access token, or empty string if none is stored. */
    override fun accessToken(): String = runBlocking { dataStore.data.first().auth.accessToken }

    override suspend fun saveGatewayAuth(gatewayAuthModel: GatewayAuthModel) {
        dataStore.updateData { prefs -> prefs.copy { auth = gatewayAuthModel.toProto() } }
    }

    override suspend fun saveUser(userModel: UserModel) {
        dataStore.updateData { prefs -> prefs.copy { user = userModel.toProto() } }
    }

    /** Wipes both auth and user data (e.g. on logout). */
    override suspend fun clear() {
        dataStore.updateData { UserPreferences.getDefaultInstance() }
    }

    /** Clears only the signed-in user, keeping the gateway token so the next login needn't re-bootstrap. */
    suspend fun clearUser() {
        dataStore.updateData { prefs -> prefs.copy { user = userProto { } } }
    }
}

// --- proto <-> domain mappers (proto3 uses empty/0 for "unset") ---

private fun AuthProto.toAuthModel(): GatewayAuthModel = GatewayAuthModel(
    accessToken = accessToken.ifEmpty { null },
    accessTokenLifeTime = accessTokenLifeTime.takeIf { it != 0 },
    accessTokenExpireDate = accessTokenExpireDate.takeIf { it != 0L }?.let(::Date)
)

private fun GatewayAuthModel.toProto(): AuthProto = authProto {
    accessToken = this@toProto.accessToken.orEmpty()
    accessTokenLifeTime = this@toProto.accessTokenLifeTime ?: 0
    accessTokenExpireDate = this@toProto.accessTokenExpireDate?.time ?: 0L
}

private fun UserProto.toUserModel(): UserModel = UserModel(
    userName = userName.ifEmpty { null },
    fullName = fullName.ifEmpty { null },
    role = role.ifEmpty { null }
)

private fun UserModel.toProto(): UserProto = userProto {
    userName = this@toProto.userName.orEmpty()
    fullName = this@toProto.fullName.orEmpty()
    role = this@toProto.role.orEmpty()
}
