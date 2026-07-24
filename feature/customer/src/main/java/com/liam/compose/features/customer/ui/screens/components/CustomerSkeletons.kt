package com.liam.compose.features.customer.ui.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.liam.compose.core.components.SkeletonBox
import com.liam.compose.core.components.SkeletonCircle
import com.liam.compose.core.components.SkeletonLine
import com.liam.compose.features.customer.R
import com.liam.compose.features.customer.ui.screens.ListBottomPadding

/**
 * Loading placeholders for the customer screens.
 *
 * Each mirrors the geometry of the content it stands in for — [CustomerListRow] for the list, the
 * form's [SectionCard]s for the form — so the real data lands in the shapes the placeholder was
 * already holding instead of shifting the layout underneath the user.
 *
 * Placeholder blocks carry no text, so each skeleton root describes the wait via `semantics` for
 * screen readers, replacing the caption that sat under the old spinner.
 */

/** Enough rows to fill any phone viewport; the surplus is clipped rather than scrolled. */
private const val LIST_SKELETON_ROWS = 6

/** Fields shown per placeholder section — the real sections hold three each. */
private const val FORM_SKELETON_FIELDS = 3

private val FieldHeight = 56.dp
private val SaveButtonHeight = 52.dp
private val StatusPillWidth = 96.dp
private val StatusPillHeight = 24.dp
private val RowActionWidth = 132.dp
private val RowActionHeight = 20.dp
private val DetailIconSize = 16.dp

/**
 * Placeholder list — [LIST_SKELETON_ROWS] cards shaped like [CustomerListRow].
 *
 * A LazyColumn rather than a plain Column, on the same content padding as the real list: it composes
 * only the rows that fit and clips the rest, so a tall placeholder can never spill over the bottom
 * bar the way an overflowing Column would. Scrolling is off — there is nothing under there yet.
 */
@Composable
internal fun CustomerListSkeleton(modifier: Modifier = Modifier) {
    val loadingLabel = stringResource(R.string.customer_list_loading)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) { contentDescription = loadingLabel },
        contentPadding = PaddingValues(
            start = ScreenPadding,
            end = ScreenPadding,
            top = 4.dp,
            bottom = ListBottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false,
    ) {
        items(LIST_SKELETON_ROWS) {
            CustomerRowSkeleton()
        }
    }
}

@Composable
private fun CustomerRowSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                SkeletonCircle(size = 44.dp)
                Spacer(Modifier.size(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    SkeletonLine(widthFraction = 0.62f, height = 15.dp)
                    Spacer(Modifier.height(6.dp))
                    SkeletonLine(widthFraction = 0.28f, height = 11.dp)
                    Spacer(Modifier.height(12.dp))
                    DetailLineSkeleton(widthFraction = 0.46f)
                    Spacer(Modifier.height(6.dp))
                    DetailLineSkeleton(widthFraction = 0.74f)
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .width(StatusPillWidth)
                        .height(StatusPillHeight),
                    shape = CircleShape,
                )
                Spacer(Modifier.weight(1f))
                SkeletonLine(
                    modifier = Modifier.width(RowActionWidth),
                    height = RowActionHeight,
                )
            }
        }
    }
}

/** Icon + text pairing used for the phone/address lines on a row. */
@Composable
private fun DetailLineSkeleton(widthFraction: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SkeletonBox(
            modifier = Modifier.size(DetailIconSize),
            shape = RoundedCornerShape(4.dp),
        )
        SkeletonLine(widthFraction = widthFraction, height = 11.dp)
    }
}

/**
 * Placeholder form — three [SectionCard]-shaped blocks over a placeholder save bar.
 *
 * Used for both the initial fetch and the save round-trip: in both cases the form is on screen but
 * not yet interactive, and [label] tells a screen reader which of the two is happening.
 */
@Composable
internal fun CustomerFormSkeleton(
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .semantics(mergeDescendants = true) { contentDescription = label },
    ) {
        // Scrolls like the real FormBody so the placeholder sections can never overflow the save
        // bar on a short screen.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            repeat(FORM_SKELETON_SECTIONS) {
                FormSectionSkeleton()
            }
        }

        SaveBarSkeleton()
    }
}

/** The real form has basic / contact / other. */
private const val FORM_SKELETON_SECTIONS = 3

@Composable
private fun FormSectionSkeleton() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCorner),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
            SkeletonLine(widthFraction = 0.36f, height = 12.dp)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(CardSpacing)) {
                repeat(FORM_SKELETON_FIELDS) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FieldHeight),
                        shape = RoundedCornerShape(FieldCorner),
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveBarSkeleton() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 2.dp,
    ) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = 14.dp)
                .height(SaveButtonHeight),
            shape = RoundedCornerShape(CardCorner),
        )
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun CustomerListSkeletonPreview() {
    MaterialTheme {
        CustomerListSkeleton()
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun CustomerFormSkeletonPreview() {
    MaterialTheme {
        CustomerFormSkeleton(label = "")
    }
}
