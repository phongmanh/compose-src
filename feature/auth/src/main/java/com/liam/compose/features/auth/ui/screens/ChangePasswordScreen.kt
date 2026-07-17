package com.liam.compose.features.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liam.compose.core.components.AppButton
import com.liam.compose.features.auth.R
import com.liam.compose.features.auth.ui.viewmodel.AuthViewModel

@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSuccess: () -> Unit = {},
    username: String = ""
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val loginState by viewModel.loginUiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) onSuccess()
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
                    ChangePasswordForm(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        onOldPasswordChange = { oldPassword = it },
                        onNewPasswordChange = { newPassword = it },
                        onConfirmPasswordChange = { confirmPassword = it },
                        onChangeClick = {
                            if (newPassword == confirmPassword) {
                                viewModel.changePassword(username, oldPassword, newPassword)
                            }
                        },
                        errorMessage = loginState.errorMessage,
                        isLoading = loginState.isLoading,
                        passwordMismatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
                    )
                }
            }
        }
    }
}

@Composable
fun ChangePasswordForm(
    oldPassword: String,
    newPassword: String,
    confirmPassword: String,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangeClick: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    passwordMismatch: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.change_password_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = oldPassword,
            onValueChange = onOldPasswordChange,
            label = { Text(stringResource(R.string.change_password_current)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            label = { Text(stringResource(R.string.change_password_new)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text(stringResource(R.string.change_password_confirm)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )

        if (passwordMismatch) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.change_password_mismatch),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

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

        Spacer(modifier = Modifier.height(32.dp))

        AppButton(
            text = stringResource(R.string.change_password_title),
            onClick = onChangeClick,
            enabled = oldPassword.isNotEmpty() && newPassword.isNotEmpty() &&
                    confirmPassword.isNotEmpty() && newPassword == confirmPassword && !isLoading,
            loading = isLoading
        )
    }
}
