package com.liam.compose.core.datastore

import androidx.datastore.core.DataStoreFactory
import com.liam.compose.features.auth.data.model.UserModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Reproduces the reported bug: "UserModel of datastore does not fire in MainViewModel when logout
 * is performed." MainViewModel.uiState is `userModel.map { ... }` with an ACTIVE collector, so this
 * test keeps an active collector on [UserPreferencesRepository.userModel] across a login (saveUser)
 * then logout (clearUser) and records every emission.
 */
class UserPreferencesRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun newRepo(scope: CoroutineScope): UserPreferencesRepository {
        val file = File(tmp.newFolder(), "user_prefs.pb")
        val dataStore = DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            scope = scope,
            produceFile = { file },
        )
        return UserPreferencesRepository(dataStore)
    }

    @Test
    fun userModel_emits_after_clearUser() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val repo = newRepo(scope)

        val userNames = CopyOnWriteArrayList<String?>()
        val collector = launch(Dispatchers.Unconfined) {
            repo.userModel.collect { userNames.add(it.userName) }
        }

        delay(300)                                   // initial emission (no user)
        repo.saveUser(UserModel(userName = "john"))  // "login"
        delay(300)
        repo.clearUser()                             // "logout"
        delay(300)

        collector.cancel()
        scope.cancel()

        println("USERNAME EMISSIONS = $userNames")

        val johnIndex = userNames.indexOf("john")
        assertTrue("login should have emitted a non-null userName", johnIndex >= 0)
        assertTrue(
            "logout should emit a null userName AFTER login — this is the flow MainViewModel observes",
            userNames.drop(johnIndex + 1).contains(null),
        )
        // Sanity: last observed state is logged-out.
        assertEquals(null, userNames.last())
    }

    /**
     * Replicates MainViewModel.uiState EXACTLY: userModel -> map to an auth state ->
     * stateIn(scope, WhileSubscribed(5000), Loading), collected by a single continuous subscriber
     * (as collectAsStateWithLifecycle does while the Activity is STARTED). Proves the
     * map + stateIn + WhileSubscribed layer flips Authenticated -> Unauthenticated on logout.
     */
    @Test
    fun uiState_flips_to_unauthenticated_on_clearUser() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val repo = newRepo(scope)

        val uiState = repo.userModel
            .map { if (it.userName != null) "Authenticated" else "Unauthenticated" }
            .stateIn(scope, SharingStarted.WhileSubscribed(5_000), "Loading")

        val states = CopyOnWriteArrayList<String>()
        val subscriber = launch(Dispatchers.Unconfined) { uiState.collect { states.add(it) } }

        delay(300)
        repo.saveUser(UserModel(userName = "john")) // login
        delay(300)
        repo.clearUser()                            // logout
        delay(300)

        subscriber.cancel()
        scope.cancel()

        println("UI STATES = $states")
        assertEquals("Authenticated", states[states.indexOf("Authenticated")])
        assertEquals(
            "uiState must end Unauthenticated after logout",
            "Unauthenticated",
            states.last(),
        )
    }
}
