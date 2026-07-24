package com.liam.compose.features.customer.ui.screens.components

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.liam.compose.features.customer.R
import com.liam.compose.features.customer.data.model.OptionModel
import com.liam.compose.features.customer.ui.state.FilterState
import com.liam.compose.features.customer.ui.state.VisitStatusFilter
import com.liam.compose.features.customer.util.toApiDate
import com.liam.compose.features.customer.util.toUtcMillisOrNull

/**
 * The chip row that opens each filter picker.
 *
 * Stateless with respect to [filter]: every change is reported through [onFilterChange] and comes
 * back down as new state, so the chips can never disagree with the query that produced the list.
 * The only local state is which picker dialog is currently open.
 *
 * The keyword field is a sibling ([CustomerSearchField]) rather than part of this composable — it
 * is rendered inside the screen's gradient header, where the chips would not have enough contrast.
 */
@Composable
fun FilterBar(
    filter: FilterState,
    sellers: List<OptionModel>,
    routes: List<OptionModel>,
    onFilterChange: (FilterState) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeDialog by remember { mutableStateOf<FilterDialog?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterValueChip(
                label = filter.seller?.text ?: stringResource(R.string.customer_filter_seller),
                selected = filter.seller != null,
                onClick = { activeDialog = FilterDialog.SELLER },
            )
            FilterValueChip(
                label = filter.route?.text ?: stringResource(R.string.customer_filter_route),
                selected = filter.route != null,
                onClick = { activeDialog = FilterDialog.ROUTE },
            )
            FilterValueChip(
                label = if (filter.status == VisitStatusFilter.ALL) {
                    stringResource(R.string.customer_filter_status)
                } else {
                    stringResource(filter.status.labelRes)
                },
                selected = filter.status != VisitStatusFilter.ALL,
                onClick = { activeDialog = FilterDialog.STATUS },
            )
            FilterValueChip(
                label = filter.dateVisit ?: stringResource(R.string.customer_filter_date),
                selected = filter.dateVisit != null,
                onClick = { activeDialog = FilterDialog.DATE },
            )
            if (filter.hasActiveFilters) {
                ClearFiltersChip(
                    label = stringResource(
                        R.string.customer_filter_clear,
                        filter.activeFilterCount,
                    ),
                    onClick = { onFilterChange(filter.clearFilters()) },
                )
            }
        }
    }

    when (activeDialog) {
        FilterDialog.SELLER -> OptionPickerDialog(
            title = stringResource(R.string.customer_filter_seller),
            options = sellers,
            selected = filter.seller,
            onSelect = {
                onFilterChange(filter.copy(seller = it))
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )

        FilterDialog.ROUTE -> OptionPickerDialog(
            title = stringResource(R.string.customer_filter_route),
            options = routes,
            selected = filter.route,
            onSelect = {
                onFilterChange(filter.copy(route = it))
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )

        FilterDialog.STATUS -> StatusPickerDialog(
            selected = filter.status,
            onSelect = {
                onFilterChange(filter.copy(status = it))
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )

        FilterDialog.DATE -> VisitDatePickerDialog(
            selectedDate = filter.dateVisit,
            onSelect = {
                onFilterChange(filter.copy(dateVisit = it))
                activeDialog = null
            },
            onDismiss = { activeDialog = null },
        )

        null -> Unit
    }
}

/** Which picker is open. Kept private — callers only ever see [FilterState] changes. */
private enum class FilterDialog { SELLER, ROUTE, STATUS, DATE }

/**
 * Rounded, elevated search field designed to sit on the screen's gold header.
 *
 * Drawn on an opaque surface rather than an outlined box: against the gradient an outline reads as
 * a hairline, while the raised card keeps the field legible in both themes.
 */
@Composable
fun CustomerSearchField(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SearchCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        TextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            placeholder = {
                Text(
                    text = stringResource(R.string.customer_search_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (keyword.isNotEmpty()) {
                    IconButton(onClick = { onKeywordChange("") }) {
                        Icon(
                            Icons.Filled.Clear,
                            contentDescription = stringResource(R.string.customer_search_clear),
                        )
                    }
                }
            },
            // The Surface supplies the shape and background; the field only draws its content.
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        )
    }
}

/** Filter entry point. The trailing caret marks it as opening a picker, not toggling a value. */
@Composable
private fun FilterValueChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        shape = RoundedCornerShape(ChipCorner),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = ChipLabelMaxWidth),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

/** Destructive-ish reset, so it is tinted apart from the picker chips rather than looking selected. */
@Composable
private fun ClearFiltersChip(
    label: String,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        shape = RoundedCornerShape(ChipCorner),
        label = {
            Text(text = label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = MaterialTheme.colorScheme.error,
            leadingIconContentColor = MaterialTheme.colorScheme.error,
        ),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
        ),
    )
}

/**
 * Single-choice picker over [options] with an in-list search box — seller and route lists come back
 * long enough that scrolling alone is not usable. Selecting the "all" row clears the filter.
 */
@Composable
private fun OptionPickerDialog(
    title: String,
    options: List<OptionModel>,
    selected: OptionModel?,
    onSelect: (OptionModel?) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val visibleOptions = remember(options, query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) options
        else options.filter { it.text.contains(trimmed, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.customer_filter_search_option)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                )

                if (visibleOptions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.customer_filter_no_option),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = OPTION_LIST_MAX_HEIGHT)) {
                        item {
                            ChoiceRow(
                                label = stringResource(R.string.customer_filter_all),
                                selected = selected == null,
                                onClick = { onSelect(null) },
                            )
                        }
                        items(visibleOptions, key = { it.value }) { option ->
                            ChoiceRow(
                                label = option.text,
                                selected = option.value == selected?.value,
                                onClick = { onSelect(option) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.customer_filter_close))
            }
        },
    )
}

@Composable
private fun StatusPickerDialog(
    selected: VisitStatusFilter,
    onSelect: (VisitStatusFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.customer_filter_status)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                VisitStatusFilter.entries.forEach { status ->
                    ChoiceRow(
                        label = stringResource(status.labelRes),
                        selected = status == selected,
                        onClick = { onSelect(status) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.customer_filter_close))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisitDatePickerDialog(
    selectedDate: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toUtcMillisOrNull(),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onSelect(datePickerState.selectedDateMillis?.toApiDate()) },
            ) {
                Text(stringResource(R.string.customer_dialog_ok))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onSelect(null) }) {
                    Text(stringResource(R.string.customer_dialog_clear))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.customer_dialog_cancel))
                }
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@get:StringRes
private val VisitStatusFilter.labelRes: Int
    get() = when (this) {
        VisitStatusFilter.ALL -> R.string.customer_filter_all
        VisitStatusFilter.NOT_YET -> R.string.customer_status_not_yet
        VisitStatusFilter.VISITED -> R.string.customer_status_visited
    }

private val OPTION_LIST_MAX_HEIGHT = 320.dp
private val SearchCorner = 18.dp
private val ChipCorner = 14.dp

/** Keeps a long seller or route name from stretching one chip across the whole row. */
private val ChipLabelMaxWidth = 140.dp
