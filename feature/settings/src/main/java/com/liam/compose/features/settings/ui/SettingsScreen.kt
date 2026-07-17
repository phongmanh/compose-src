package com.liam.compose.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liam.compose.core.components.AppButton
import com.liam.compose.core.components.AppOutlinedButton
import com.liam.compose.features.settings.R
import com.liam.compose.core.navigation.LocalNavBackStack
import com.liam.compose.core.navigation.navigate
import com.liam.compose.features.settings.navigation.SettingKey

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    val backStack = LocalNavBackStack.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        val displayName = user.fullName ?: user.userName
        if (displayName != null) {
            Text(
                text = stringResource(R.string.settings_signed_in_as, displayName),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp),
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        AppButton(
            text = stringResource(R.string.settings_change_password),
            onClick = { backStack.navigate(SettingKey.ChangePassword(user.userName.orEmpty())) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppOutlinedButton(
            text = stringResource(R.string.settings_logout),
            onClick = { viewModel.logout() }
        )
    }
}
