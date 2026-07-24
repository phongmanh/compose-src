package com.liam.compose.features.customer.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared visual vocabulary for the customer screens.
 *
 * The tokens mirror the Home screen's language (gold gradient header, tinted icon chips, elevated
 * rounded surfaces) so the two features read as one app. They are kept local to the feature for the
 * same reason Home keeps its own: there is no design-system module holding shape/elevation tokens
 * yet, only the Material colour scheme and typography.
 */

// --- Layout tokens ---
internal val ScreenPadding = 20.dp
internal val HeaderCorner = 28.dp
internal val CardCorner = 20.dp
internal val FieldCorner = 16.dp
internal val CardSpacing = 12.dp

// --- Brand accents for the header (not part of the Material scheme) ---
internal val HeaderGradient = listOf(Color(0xFFEAD283), Color(0xFFD2B94E))
internal val HeaderContent = Color(0xFF3B2F0B) // dark-on-gold for real contrast
internal val HeaderContentMuted = HeaderContent.copy(alpha = 0.72f)

// --- Visit status accents ---
internal val VisitedAccent = Color(0xFF12A594)
internal val NotYetAccent = Color(0xFFF2812B)

/**
 * Accents the row avatar cycles through so a long list has visual rhythm instead of a wall of
 * identical circles. Picked by name hash, so a given customer keeps the same colour across
 * refreshes and pages.
 */
private val AvatarAccents = listOf(
    Color(0xFF2481E5),
    Color(0xFF12A594),
    Color(0xFF7C5CFC),
    Color(0xFFF2812B),
    Color(0xFFE0518A),
    Color(0xFF4C6FE0),
)

internal fun avatarAccentFor(seed: String): Color =
    AvatarAccents[(seed.hashCode().mod(AvatarAccents.size))]

/**
 * Full-bleed gold header with rounded lower corners — the anchor of every customer screen.
 *
 * Paints behind the status bar (the host Scaffold deliberately does not consume the top inset) and
 * pushes its own content below it.
 */
@Composable
internal fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = HeaderCorner, bottomEnd = HeaderCorner))
            .background(Brush.verticalGradient(HeaderGradient))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = ScreenPadding, end = ScreenPadding, top = 18.dp, bottom = 18.dp),
        content = content,
    )
}

/** Soft tinted circle behind an icon — the accent motif carried over from the Home screen. */
@Composable
internal fun IconChip(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = ChipTintAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = accent,
            modifier = Modifier.size(iconSize),
        )
    }
}

/** Same chip, but showing a customer's initial instead of an icon. */
@Composable
internal fun InitialChip(
    initial: String?,
    accent: Color,
    fallbackIcon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    if (initial == null) {
        IconChip(icon = fallbackIcon, accent = accent, modifier = modifier, size = size)
        return
    }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = ChipTintAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accent,
        )
    }
}

/** Compact tinted badge used for visit status — reads at a glance where plain text did not. */
@Composable
internal fun StatusPill(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(accent.copy(alpha = PillTintAlpha))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = accent,
        )
    }
}

/** Titled card grouping related content. Used for the form's field sections. */
@Composable
internal fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(CardSpacing), content = content)
        }
    }
}

/** Icon + text line used for the secondary details on a customer row. */
@Composable
internal fun DetailLine(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
    maxLines: Int = 1,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (muted) MutedAlpha else 1f,
            ),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (muted) MutedAlpha else 1f,
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val ChipTintAlpha = 0.14f
private const val PillTintAlpha = 0.16f

/** Placeholder details ("no phone yet") sit back so real data reads first. */
private const val MutedAlpha = 0.55f
