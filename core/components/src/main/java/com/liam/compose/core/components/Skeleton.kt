package com.liam.compose.core.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Skeleton placeholders — muted blocks shaped like the content that is still loading, lit by a band
 * sweeping across them.
 *
 * The sweep is positioned in **window** coordinates, not per-element: each placeholder subtracts its
 * own x offset from the shared animation, so one continuous band crosses the whole screen instead of
 * every block animating on its own clock. That is what makes a group of blocks read as a single
 * loading surface rather than a pile of blinking rectangles.
 *
 * Skeletons carry no text, so a screen reader gets nothing from them. Give the screen-level skeleton
 * a `semantics { contentDescription = ... }` describing the wait — see the feature skeletons.
 */

private const val SWEEP_DURATION_MILLIS = 1_200
private val SweepWidth = 220.dp
private val SkeletonCorner = 8.dp

/** Opacity of the resting block, and of the band passing through it, per theme. */
private const val BLOCK_ALPHA_LIGHT = 0.11f
private const val BAND_ALPHA_LIGHT = 0.04f
private const val BLOCK_ALPHA_DARK = 0.10f
private const val BAND_ALPHA_DARK = 0.22f

/** Below this surface luminance the scheme is treated as dark. */
private const val DARK_SURFACE_LUMINANCE = 0.5f

/** Paints [shape] as a shimmering placeholder block. The element supplies its own size. */
@Composable
fun Modifier.shimmer(shape: Shape = RoundedCornerShape(SkeletonCorner)): Modifier {
    val scheme = MaterialTheme.colorScheme
    // The band always moves *towards the background colour*, so it has to flip with the theme:
    // less opaque than the block on light, more opaque on dark. Derived from the scheme's own
    // luminance rather than isSystemInDarkTheme() so it stays correct for any scheme override.
    val isDark = scheme.surface.luminance() < DARK_SURFACE_LUMINANCE
    val block = scheme.onSurface.copy(
        alpha = if (isDark) BLOCK_ALPHA_DARK else BLOCK_ALPHA_LIGHT,
    )
    val band = scheme.onSurface.copy(
        alpha = if (isDark) BAND_ALPHA_DARK else BAND_ALPHA_LIGHT,
    )

    val sweepWidthPx = with(LocalDensity.current) { SweepWidth.toPx() }
    val windowWidthPx = LocalWindowInfo.current.containerSize.width.toFloat()

    val progress by rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(SWEEP_DURATION_MILLIS, easing = LinearEasing),
        ),
        label = "sweep",
    )

    var xInWindow by remember { mutableFloatStateOf(0f) }

    return this
        .clip(shape)
        .onGloballyPositioned { xInWindow = it.positionInWindow().x }
        .drawBehind {
            // Fall back to the element's own width if the window has not been measured yet, so the
            // block is never left unpainted.
            val span = if (windowWidthPx > 0f) windowWidthPx else size.width
            val travel = span + sweepWidthPx * 2
            val start = progress * travel - sweepWidthPx - xInWindow
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(block, band, block),
                    start = Offset(start, 0f),
                    end = Offset(start + sweepWidthPx, 0f),
                ),
            )
        }
}

/** One placeholder block. Size it through [modifier]. */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(SkeletonCorner),
) {
    Box(modifier = modifier.shimmer(shape))
}

/**
 * Placeholder for a line of text. [widthFraction] mirrors how full the real line is — varying it
 * between neighbouring lines is what stops a skeleton looking like a barcode.
 */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    widthFraction: Float = 1f,
    height: Dp = 12.dp,
) {
    SkeletonBox(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height),
        shape = RoundedCornerShape(height / 2),
    )
}

/** Placeholder for a circular element — an avatar or one of the tinted icon chips. */
@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    SkeletonBox(modifier = modifier.size(size), shape = CircleShape)
}
