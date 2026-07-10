package com.liam.compose.features.home.ui

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RealEstateAgent
import androidx.compose.material.icons.outlined.StackedLineChart
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liam.compose.features.home.R
import com.liam.compose.features.auth.data.model.UserModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- Layout tokens (kept local; the feature has no design-system module yet) ---
private val ScreenPadding = 20.dp
private val HeroCorner = 28.dp
private val CardCorner = 20.dp
private val TileCorner = 20.dp
private val GridSpacing = 14.dp
private const val GridColumns = 3

// --- Brand accents for the hero + payment highlight (not part of the Material scheme) ---
private val HeroGradient = listOf(Color(0xFFEAD283), Color(0xFFD2B94E))
private val PaymentGradient = listOf(Color(0xFFF9A63F), Color(0xFFF15A38))
private val HeroContent = Color(0xFF3B2F0B)              // dark-on-gold for real contrast
private val HeroContentMuted = HeroContent.copy(alpha = 0.72f)
private val NotificationBadge = Color(0xFFE53935)

/** One tappable action in the home grid, each with its own accent so the grid isn't monotone. */
private data class HomeAction(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val accent: Color,
)

private val gridActions = listOf(
    HomeAction(Icons.Outlined.StackedLineChart, R.string.home_action_overview, Color(0xFF2481E5)),
    HomeAction(Icons.Outlined.Group, R.string.home_action_customers, Color(0xFF12A594)),
    HomeAction(Icons.Outlined.Handshake, R.string.home_action_contracts, Color(0xFF7C5CFC)),
    HomeAction(Icons.AutoMirrored.Outlined.Login, R.string.home_action_attendance, Color(0xFFF2812B)),
    HomeAction(Icons.Outlined.RealEstateAgent, R.string.home_action_advance, Color(0xFFE0518A)),
    HomeAction(Icons.Outlined.Payments, R.string.home_action_refund, Color(0xFF4C6FE0)),
)

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(state)
}

@Composable
private fun HomeContent(uiState: HomeUiState) {
    when (uiState) {
        is HomeUiState.Loading -> StatusMessage(loading = true)
        is HomeUiState.Success -> HomeDetail(uiState.data)
        is HomeUiState.Error -> StatusMessage(loading = false)
    }
}

@Composable
private fun StatusMessage(loading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(if (loading) R.string.home_loading else R.string.home_error),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HomeDetail(data: UserModel) {
    val displayName = data.fullName?.takeIf { it.isNotBlank() }
        ?: data.userName?.takeIf { it.isNotBlank() }
        ?: ""
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }
    val greeting = stringResource(greetingResFor(remember { Calendar.getInstance() }))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(greeting = greeting, name = displayName)

        // Pull the body up so the attendance card overlaps the hero's rounded lower edge.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-48).dp)
                .padding(horizontal = ScreenPadding),
        ) {
            AttendanceCard(date = today)

            Spacer(Modifier.height(26.dp))

            Text(
                text = stringResource(R.string.home_section_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(GridSpacing))

            ActionGrid()

            Spacer(Modifier.height(GridSpacing))

            PaymentCta()

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroHeader(greeting: String, name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = HeroCorner, bottomEnd = HeroCorner))
            .background(Brush.verticalGradient(HeroGradient))
            // Gradient fills behind the status bar; push the hero's content below it.
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = ScreenPadding, end = ScreenPadding, top = 22.dp, bottom = 64.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = name)
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HeroContentMuted,
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = HeroContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            NotificationButton()
        }
    }
}

@Composable
private fun Avatar(name: String) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString()
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        if (initial != null) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = HeroContent,
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.home_avatar_placeholder),
                tint = HeroContent,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun NotificationButton() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.28f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = stringResource(R.string.home_notifications),
            tint = HeroContent,
            modifier = Modifier.size(24.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(11.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(NotificationBadge),
        )
    }
}

@Composable
private fun AttendanceCard(date: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconChip(icon = Icons.Outlined.CalendarMonth, accent = Color(0xFF2481E5))
                Spacer(Modifier.size(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.home_attendance_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeStat(
                    icon = Icons.Outlined.Timer,
                    label = stringResource(R.string.home_check_in),
                    accent = Color(0xFF12A594),
                    modifier = Modifier.weight(1f),
                )
                VerticalDivider(
                    modifier = Modifier.height(34.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                TimeStat(
                    icon = Icons.Outlined.TimerOff,
                    label = stringResource(R.string.home_check_out),
                    accent = Color(0xFFF2812B),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun TimeStat(
    icon: ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconChip(icon = icon, accent = accent, size = 40.dp, iconSize = 22.dp)
        Spacer(Modifier.size(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.home_time_placeholder),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Soft tinted circle behind an icon — the recurring accent motif across the screen. */
@Composable
private fun IconChip(
    icon: ImageVector,
    accent: Color,
    size: Dp = 44.dp,
    iconSize: Dp = 24.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun ActionGrid() {
    val rows = gridActions.chunked(GridColumns)
    Column {
        rows.forEachIndexed { index, rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GridSpacing),
            ) {
                rowActions.forEach { action ->
                    ActionTile(action = action, modifier = Modifier.weight(1f))
                }
                // Keep the last (possibly short) row aligned to the column grid.
                repeat(GridColumns - rowActions.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            if (index < rows.lastIndex) Spacer(Modifier.height(GridSpacing))
        }
    }
}

@Composable
private fun ActionTile(action: HomeAction, modifier: Modifier = Modifier) {
    Surface(
        onClick = { /* TODO: navigate to ${action.labelRes} once destinations exist */ },
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(TileCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            IconChip(icon = action.icon, accent = action.accent)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(action.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PaymentCta() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(PaymentGradient))
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_action_payment),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.home_payment_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@StringRes
private fun greetingResFor(calendar: Calendar): Int =
    when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..10 -> R.string.home_greeting_morning
        in 11..17 -> R.string.home_greeting_afternoon
        else -> R.string.home_greeting_evening
    }

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeDetailPreview() {
    MaterialTheme {
        HomeDetail(UserModel(userName = "nguyenvana", fullName = "Nguyễn Văn A", role = "Quản lý"))
    }
}

@Preview(showBackground = true, heightDp = 780, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeDetailDarkPreview() {
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
        HomeDetail(UserModel(userName = "nguyenvana", fullName = "Nguyễn Văn A", role = "Quản lý"))
    }
}
