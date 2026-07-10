package com.liam.compose.features.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liam.compose.features.auth.R
import com.liam.compose.features.auth.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit = {}
) {

    val userNameState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()

    val loginState by viewModel.loginUiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) onLoginSuccess()
    }

    // AuthViewModel outlives this screen: the nav-entry viewmodel store keeps it across the
    // login -> app -> logout swap. Clear its one-shot result when we leave (the handoff into the
    // app) so returning to Login after logout shows the form, not the stale success spinner.
    DisposableEffect(Unit) {
        onDispose { viewModel.resetLoginState() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                loginState.isLoading || loginState.isSuccess -> {
                    CircularProgressIndicator()
                }

                else -> {
                    LoginForm(
                        userNameState = userNameState,
                        passwordState = passwordState,
                        onLoginClick = {
                            viewModel.login(
                                userNameState.text.toString(),
                                passwordState.text.toString()
                            )
                        },
                        errorMessage = loginState.errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    userNameState: TextFieldState,
    passwordState: TextFieldState,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {},
    errorMessage: String? = null,
    isLoading: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // derivedStateOf recomputes on each edit but only notifies the button when the result
    // flips (empty <-> non-empty), so typing no longer recomposes the form on every keystroke.
    val canSubmit by remember {
        derivedStateOf {
            userNameState.text.isNotEmpty() && passwordState.text.isNotEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Logo / branding
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.login_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_welcome_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            state = userNameState,
            label = { Text(stringResource(R.string.login_username)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            lineLimits = TextFieldLineLimits.SingleLine
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedSecureTextField(
            state = passwordState,
            label = { Text(stringResource(R.string.login_password)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            textObfuscationMode = if (passwordVisible) TextObfuscationMode.Visible else TextObfuscationMode.Hidden,
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = {
                    passwordVisible = !passwordVisible
                }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Forgot password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onForgotPassword, enabled = !isLoading) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = canSubmit && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .height(20.dp)
                        .padding(end = 8.dp)
                )
            }
            Text(stringResource(R.string.login_sign_in))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign up
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onSignUp) {
                Text(
                    text = stringResource(R.string.login_sign_up),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
