package com.liam.compose.features.home.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.liam.compose.core.components.SkeletonBox
import com.liam.compose.core.components.SkeletonCircle
import com.liam.compose.core.components.SkeletonLine
import com.liam.compose.features.home.R

/**
 * Loading placeholder for [HomeScreen], laid out on the same geometry as `HomeDetail` so the real
 * content drops straight into the shapes the skeleton was holding.
 *
 * The gold hero keeps its gradient rather than turning grey — it is brand furniture that is never
 * "loading", and flashing it to grey and back is the flicker a skeleton exists to avoid.
 */
@Composable
internal fun HomeSkeleton(modifier: Modifier = Modifier) {
    // Placeholder blocks are invisible to a screen reader, so the surface as a whole announces the
    // wait that the spinner's caption used to.
    val loadingLabel = stringResource(R.string.home_loading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Scrolls like the real body: on a short screen the placeholder is as tall as the
            // content it stands in for, and must not spill past the viewport.
            .verticalScroll(rememberScrollState())
            .semantics(mergeDescendants = true) { contentDescription = loadingLabel },
    ) {
        HeroSkeleton()

        // Same -48dp pull as the real body, so the attendance card overlaps the hero identically.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-48).dp)
                .padding(horizontal = ScreenPadding),
        ) {
            AttendanceCardSkeleton()

            Spacer(Modifier.height(26.dp))
            SkeletonLine(widthFraction = 0.30f, height = 14.dp)
            Spacer(Modifier.height(GridSpacing))

            ActionGridSkeleton()

            Spacer(Modifier.height(GridSpacing))

            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PaymentCtaHeight),
                shape = RoundedCornerShape(PaymentCtaCorner),
            )
        }
    }
}

@Composable
private fun HeroSkeleton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = HeroCorner, bottomEnd = HeroCorner))
            .background(Brush.verticalGradient(HeroGradient))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = ScreenPadding, end = ScreenPadding, top = 22.dp, bottom = 64.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonCircle(size = 48.dp)
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonLine(widthFraction = 0.34f, height = 11.dp)
                Spacer(Modifier.height(8.dp))
                SkeletonLine(widthFraction = 0.58f, height = 16.dp)
            }
            Spacer(Modifier.size(14.dp))
            SkeletonCircle(size = 44.dp)
        }
    }
}

@Composable
private fun AttendanceCardSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonCircle(size = 44.dp)
                Spacer(Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonLine(widthFraction = 0.42f, height = 10.dp)
                    Spacer(Modifier.height(7.dp))
                    SkeletonLine(widthFraction = 0.30f, height = 14.dp)
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeStatSkeleton(modifier = Modifier.weight(1f))
                Spacer(Modifier.size(16.dp))
                TimeStatSkeleton(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TimeStatSkeleton(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        SkeletonCircle(size = 40.dp)
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonLine(widthFraction = 0.62f, height = 10.dp)
            Spacer(Modifier.height(7.dp))
            SkeletonLine(widthFraction = 0.45f, height = 14.dp)
        }
    }
}

/** Mirrors the real grid: [GRID_COLUMNS] per row, as many rows as there are actions. */
@Composable
private fun ActionGridSkeleton() {
    val rows = gridActions.size.chunkedRowSizes()
    Column {
        rows.forEachIndexed { index, rowSize ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GridSpacing),
            ) {
                repeat(rowSize) {
                    ActionTileSkeleton(modifier = Modifier.weight(1f))
                }
                repeat(GRID_COLUMNS - rowSize) {
                    Spacer(Modifier.weight(1f))
                }
            }
            if (index < rows.lastIndex) Spacer(Modifier.height(GridSpacing))
        }
    }
}

@Composable
private fun ActionTileSkeleton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(ActionTileHeight),
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
            SkeletonCircle(size = 44.dp)
            Spacer(Modifier.height(12.dp))
            SkeletonLine(widthFraction = 0.72f, height = 10.dp)
        }
    }
}

private val ActionTileHeight = 110.dp
private val PaymentCtaHeight = 84.dp
private val PaymentCtaCorner = 22.dp

/** Row sizes for [gridActions], the last row possibly short. */
private fun Int.chunkedRowSizes(): List<Int> =
    List((this + GRID_COLUMNS - 1) / GRID_COLUMNS) { row ->
        minOf(GRID_COLUMNS, this - row * GRID_COLUMNS)
    }

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeSkeletonPreview() {
    MaterialTheme {
        HomeSkeleton()
    }
}

@Preview(
    showBackground = true,
    heightDp = 780,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeSkeletonDarkPreview() {
    MaterialTheme(colorScheme = androidx.compose.material3.darkColorScheme()) {
        HomeSkeleton()
    }
}
