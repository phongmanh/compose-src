package com.liam.compose.entry

import androidx.annotation.StringRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.liam.compose.features.home.navigation.HomeKey
import com.liam.compose.features.settings.navigation.SettingKey
import com.liam.compose.R

/**
 * One entry per bottom-nav tab, each carrying the root [NavKey] for its back stack plus an
 * outlined ([icon]) and filled ([selectedIcon]) glyph so the active tab reads as "filled in".
 */
enum class BottomTab(
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val rootKey: NavKey,
) {
    Home(R.string.tab_home, Icons.Outlined.Home, Icons.Filled.Home, HomeKey.Root),
    Report(R.string.tab_report, Icons.Outlined.Report, Icons.Filled.Report, ReportKey.Root),
    Settings(R.string.tab_settings, Icons.Outlined.Settings, Icons.Filled.Settings, SettingKey.Root),
}

val bottomTabSaver: Saver<BottomTab, String> = Saver(
    save = { tab -> tab.name },
    restore = { name -> BottomTab.valueOf(name) },
)

@Composable
fun AppBottomBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    // Float the bar above content on a soft shadow with rounded top corners, matching the
    // surface cards on the Home screen.
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 12.dp,
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            // Brand-orange selection with the soft tinted "IconChip" pill used across the app.
            val itemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BottomTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = if (selected) tab.selectedIcon else tab.icon,
                            contentDescription = stringResource(tab.labelRes),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(tab.labelRes),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    colors = itemColors,
                )
            }
        }
    }
}
