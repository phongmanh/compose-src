package com.liam.compose.features.customer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liam.compose.features.customer.R
import com.liam.compose.features.customer.data.model.SaveCustomerModel
import com.liam.compose.features.customer.navigation.NEW_CUSTOMER_ID
import com.liam.compose.features.customer.ui.screens.components.CardCorner
import com.liam.compose.features.customer.ui.screens.components.CustomerFormSkeleton
import com.liam.compose.features.customer.ui.screens.components.CustomerTypeField
import com.liam.compose.features.customer.ui.screens.components.DropdownField
import com.liam.compose.features.customer.ui.screens.components.GradientHeader
import com.liam.compose.features.customer.ui.screens.components.HeaderContent
import com.liam.compose.features.customer.ui.screens.components.IconChip
import com.liam.compose.features.customer.ui.screens.components.ScreenPadding
import com.liam.compose.features.customer.ui.screens.components.SectionCard
import com.liam.compose.features.customer.ui.screens.components.TextInputField
import com.liam.compose.features.customer.ui.state.CustomerFormUiState
import com.liam.compose.features.customer.ui.viewmodel.CustomerFormViewModel
import kotlinx.coroutines.launch

/**
 * Create/update form for a single customer.
 *
 * Fields are grouped into titled sections rather than one long stack, and the save action sits in a
 * fixed bottom bar so it stays reachable however far the form is scrolled.
 */
@Composable
fun CustomerFormScreen(
    customerId: Int,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var formData by remember { mutableStateOf(viewModel.getSavedDraft()) }

    val isCreate = customerId == NEW_CUSTOMER_ID
    val title = stringResource(
        if (isCreate) R.string.customer_form_create_title else R.string.customer_form_edit_title,
    )

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CustomerFormUiState.Success -> onSuccess()
            is CustomerFormUiState.Error -> snackbarHostState.showSnackbar(state.message)
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        // The header paints its own status-bar inset so the gradient can run full-bleed.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            FormHeader(title = title, onBack = onBack)

            when (val state = uiState) {
                // Fetching the form and saving it both leave the same fields on screen but
                // uninteractive, so both hold the layout with the form skeleton; only the
                // screen-reader label distinguishes them.
                is CustomerFormUiState.Idle,
                is CustomerFormUiState.Loading,
                    -> CustomerFormSkeleton(
                    label = stringResource(R.string.customer_form_loading),
                )

                is CustomerFormUiState.Saving -> CustomerFormSkeleton(
                    label = stringResource(R.string.customer_form_saving),
                )

                is CustomerFormUiState.Success -> StatusPanel(
                    message = stringResource(R.string.customer_form_saved),
                    icon = Icons.Outlined.CheckCircle,
                    accent = MaterialTheme.colorScheme.primary,
                )

                is CustomerFormUiState.Error -> StatusPanel(
                    message = state.message.ifBlank {
                        stringResource(R.string.customer_error_generic)
                    },
                    icon = Icons.Outlined.ErrorOutline,
                    accent = MaterialTheme.colorScheme.error,
                )

                is CustomerFormUiState.Initialized -> FormBody(
                    state = state,
                    formData = formData,
                    onFormDataChange = { formData = it },
                    isCreate = isCreate,
                    onSave = {
                        viewModel.updateDraft(formData)
                        coroutineScope.launch { viewModel.saveCustomer(formData) }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun FormHeader(title: String, onBack: () -> Unit) {
    GradientHeader {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.3f), CircleShape),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.customer_form_back),
                    tint = HeaderContent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.size(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = HeaderContent,
            )
        }
    }
}

@Composable
private fun FormBody(
    state: CustomerFormUiState.Initialized,
    formData: SaveCustomerModel,
    onFormDataChange: (SaveCustomerModel) -> Unit,
    isCreate: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionCard(title = stringResource(R.string.customer_form_section_basic)) {
                TextInputField(
                    value = formData.name.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(name = it)) },
                    label = stringResource(R.string.customer_form_name),
                    required = true,
                )
                TextInputField(
                    value = formData.code.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(code = it)) },
                    label = stringResource(R.string.customer_form_code),
                )
                CustomerTypeField(
                    selectedType = formData.customerTypeId,
                    onValueChange = { onFormDataChange(formData.copy(customerTypeId = it)) },
                )
            }

            SectionCard(title = stringResource(R.string.customer_form_section_contact)) {
                TextInputField(
                    value = formData.mainContactName.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(mainContactName = it)) },
                    label = stringResource(R.string.customer_form_contact),
                )
                TextInputField(
                    value = formData.phone.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(phone = it)) },
                    label = stringResource(R.string.customer_form_phone),
                    required = true,
                )
                TextInputField(
                    value = formData.address.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(address = it)) },
                    label = stringResource(R.string.customer_form_address),
                    required = true,
                    singleLine = false,
                )
            }

            SectionCard(title = stringResource(R.string.customer_form_section_other)) {
                TextInputField(
                    value = formData.taxCode.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(taxCode = it)) },
                    label = stringResource(R.string.customer_form_tax_code),
                )
                TextInputField(
                    value = formData.siSoHocSinh?.toString().orEmpty(),
                    onValueChange = {
                        onFormDataChange(formData.copy(siSoHocSinh = it.toIntOrNull()))
                    },
                    label = stringResource(R.string.customer_form_students),
                    isNumeric = true,
                )
                // Option lists arrive with the form; an empty one means the lookup failed, so the
                // field is hidden rather than shown as an empty picker.
                if (state.provinceOptions.isNotEmpty()) {
                    DropdownField(
                        label = stringResource(R.string.customer_form_province),
                        options = state.provinceOptions,
                        selectedValue = formData.provinceCode,
                        onValueChange = { onFormDataChange(formData.copy(provinceCode = it)) },
                    )
                }
                if (state.routeOptions.isNotEmpty()) {
                    DropdownField(
                        label = stringResource(R.string.customer_form_route),
                        options = state.routeOptions,
                        selectedValue = formData.routeCode,
                        onValueChange = { onFormDataChange(formData.copy(routeCode = it)) },
                    )
                }
                TextInputField(
                    value = formData.note.orEmpty(),
                    onValueChange = { onFormDataChange(formData.copy(note = it)) },
                    label = stringResource(R.string.customer_form_note),
                    singleLine = false,
                )
            }

            Text(
                text = stringResource(R.string.customer_form_required_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SaveBar(isCreate = isCreate, onSave = onSave)
    }
}

/** Fixed action bar. Raised above the form so the save button never scrolls out of reach. */
@Composable
private fun SaveBar(isCreate: Boolean, onSave: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 2.dp,
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = 14.dp)
                .height(52.dp),
            shape = RoundedCornerShape(CardCorner),
        ) {
            Text(
                text = stringResource(
                    if (isCreate) R.string.customer_button_create
                    else R.string.customer_button_save,
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Centred panel for the form's terminal states — saved or failed.
 *
 * The in-progress states (loading, saving) use `CustomerFormSkeleton` instead: they keep the form's
 * shape on screen, where these two deliberately replace it with a single verdict.
 */
@Composable
private fun StatusPanel(
    message: String,
    icon: ImageVector,
    accent: Color,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconChip(icon = icon, accent = accent, size = 72.dp, iconSize = 36.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}
