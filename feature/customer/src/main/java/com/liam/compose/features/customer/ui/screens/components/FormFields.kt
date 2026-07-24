package com.liam.compose.features.customer.ui.screens.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.KeyboardType
import com.liam.compose.features.customer.R
import com.liam.compose.features.customer.data.model.OptionModel

/**
 * Form controls for the customer form, sharing one rounded outline so the sections read as a single
 * set of inputs rather than stock Material defaults.
 */

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    isNumeric: Boolean = false,
    singleLine: Boolean = true,
    placeholder: String = "",
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (isNumeric) {
                if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                    onValueChange(newValue)
                }
            } else {
                onValueChange(newValue)
            }
        },
        label = { RequiredLabel(label = label, required = required) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FieldCorner),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumeric) KeyboardType.Number else KeyboardType.Text,
        ),
    )
}

/** Marks the asterisk in the theme's error colour so "required" survives a glance. */
@Composable
private fun RequiredLabel(label: String, required: Boolean) {
    if (!required) {
        Text(label)
        return
    }
    Text(
        buildAnnotatedString {
            append(label)
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.error)) { append(" *") }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<OptionModel>,
    selectedValue: String?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.value == selectedValue }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedOption?.text ?: "",
            onValueChange = {},
            readOnly = true,
            label = { RequiredLabel(label = label, required = required) },
            placeholder = { Text(stringResource(R.string.customer_form_select_hint)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(FieldCorner),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        // ExposedDropdownMenu (not a bare DropdownMenu) so the list is width- and position-matched
        // to the anchor field.
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.text) },
                    onClick = {
                        onValueChange(option.value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun CustomerTypeField(
    selectedType: Int?,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownField(
        label = stringResource(R.string.customer_form_type),
        options = CustomerTypes,
        selectedValue = selectedType?.toString(),
        onValueChange = { value -> value.toIntOrNull()?.let(onValueChange) },
        modifier = modifier,
    )
}

/**
 * Customer types are a fixed pair on this backend, unlike routes and provinces which are fetched.
 * Codes only, so they are not translated.
 */
private val CustomerTypes = listOf(
    OptionModel("1", "CL"),
    OptionModel("2", "DL"),
)
