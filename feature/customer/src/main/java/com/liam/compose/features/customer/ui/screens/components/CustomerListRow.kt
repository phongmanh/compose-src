package com.liam.compose.features.customer.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.liam.compose.features.customer.R
import com.liam.compose.features.customer.data.model.CustomerModel
import com.liam.compose.features.customer.ui.state.VisitStatusFilter

/**
 * One customer as an elevated card.
 *
 * Tapping anywhere on the card edits it — the whole surface is a far larger target than the icon
 * button it replaces, and "mark visited" stays a separate explicit action in the footer so the two
 * can't be confused.
 */
@Composable
fun CustomerListRow(
    customer: CustomerModel,
    onEdit: () -> Unit,
    onVisited: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVisited = customer.visitStatus == VisitStatusFilter.VISITED.apiValue
    val name = customer.customerName?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.customer_row_unknown_name)
    val accent = avatarAccentFor(customer.customerCode ?: name)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.customer_button_edit),
                onClick = onEdit,
            ),
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                InitialChip(
                    initial = name.trim().firstOrNull()?.uppercaseChar()?.toString(),
                    accent = accent,
                    fallbackIcon = Icons.Outlined.Person,
                )
                Spacer(Modifier.size(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    customer.customerCode?.takeIf { it.isNotBlank() }?.let { code ->
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = code,
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    val phone = customer.phone?.takeIf { it.isNotBlank() }
                    DetailLine(
                        icon = Icons.Outlined.Phone,
                        text = phone ?: stringResource(R.string.customer_row_no_phone),
                        muted = phone == null,
                    )
                    Spacer(Modifier.height(4.dp))

                    val address = customer.address?.takeIf { it.isNotBlank() }
                    DetailLine(
                        icon = Icons.Outlined.LocationOn,
                        text = address ?: stringResource(R.string.customer_row_no_address),
                        muted = address == null,
                        maxLines = 2,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusPill(
                    text = stringResource(
                        if (isVisited) R.string.customer_status_visited
                        else R.string.customer_status_not_yet,
                    ),
                    accent = if (isVisited) VisitedAccent else NotYetAccent,
                )
                Spacer(Modifier.weight(1f))

                // Already-visited rows keep the action visible but disabled, so the row's height
                // doesn't change as the list updates.
                TextButton(onClick = onVisited, enabled = !isVisited) {
                    if (isVisited) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                    }
                    Text(
                        text = stringResource(R.string.customer_row_mark_visited),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
