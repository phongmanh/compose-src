package com.liam.compose.entry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import com.liam.compose.core.navigation.AppNavigation
import com.liam.compose.core.navigation.rememberTabbedNavigator
import com.liam.compose.features.auth.navigation.AuthKey
import com.liam.compose.features.auth.navigation.authEntries
import com.liam.compose.core.designsystem.theme.JetpackComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize SplashScreen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetpackComposeTheme {
                val state by mainViewModel.uiState.collectAsStateWithLifecycle()
                when (state) {
                    AuthState.Authenticated -> JetpackComposeApp()
                    else -> AuthNavHost()
                }
            }
        }

        // Keep the splash screen visible for this Activity until the main ViewModel is done
        splashScreen.setKeepOnScreenCondition { mainViewModel.shouldShowFirstScreen() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.gatewayAuthState.collect { gatewayState ->
                    if (gatewayState is GatewayAuthState.Failed) {
                        // Handle failure to get gateway token if needed
                        // For example, show an error message or navigate to an error screen
                        finishAffinity()
                    }
                }
            }
        }
    }
}

/**
 * Login flow shown while the user is unauthenticated. The transition to [JetpackComposeApp] is driven by
 * [MainViewModel] observing the persisted user session, so [authEntries] only needs a no-op callback.
 */
@Composable
private fun AuthNavHost(viewModel: MainViewModel = hiltViewModel()) {
    val authBackStack = rememberNavBackStack(AuthKey.Root)
    AppNavigation(
        modifier = Modifier.fillMaxSize(),
        backStack = authBackStack,
        entries = {
            authEntries(onLoginSuccess = {
                //viewModel.onLoginSuccess()
            })
        },
    )
}

@Composable
fun JetpackComposeApp() {

    // One back stack per tab; selecting the active tab again pops it to root.
    val navigator = rememberTabbedNavigator(
        tabs = BottomTab.entries,
        rootKeyFor = { it.rootKey },
        initialTab = BottomTab.Home,
        tabSaver = bottomTabSaver,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Let the body draw behind the status bar so screens like Home can paint a full-bleed
        // header; each screen applies its own top inset. The bottom bar still handles its inset.
        contentWindowInsets = WindowInsets.systemBars.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
        ),
        bottomBar = {
            AppBottomBar(
                selectedTab = navigator.selectedTab,
                onTabSelected = navigator::select,
            )
        },
    ) { innerPadding ->
        AppNavigation(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            backStack = navigator.currentBackStack,
            entries = { getAppEntries(it) },
        )
    }
}
