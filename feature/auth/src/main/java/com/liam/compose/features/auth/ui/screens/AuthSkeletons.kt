package com.liam.compose.features.auth.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.liam.compose.core.components.SkeletonBox
import com.liam.compose.core.components.SkeletonLine
import com.liam.compose.features.auth.R

/**
 * Busy-state placeholders for the auth forms.
 *
 * These stand in for a submit round-trip rather than a content fetch: the fields are already filled
 * in, and nothing new is arriving to fill the blocks. They earn their place by holding the form's
 * exact footprint while the request is in flight — the old spinner collapsed the whole form to a
 * 48dp circle and snapped it back on failure, which is the jump a skeleton avoids.
 *
 * The blocks are wordless, so each root announces the wait for screen readers.
 */

private val LogoSize = 64.dp
private val LogoCorner = 20.dp
private val FieldHeight = 56.dp
private val FieldCorner = 4.dp
private val ButtonHeight = 48.dp
private val ButtonCorner = 24.dp
private val LinkWidth = 120.dp
private val LinkHeight = 14.dp

/** Mirrors [LoginForm] while the sign-in request is in flight. */
@Composable
internal fun LoginSkeleton(modifier: Modifier = Modifier) {
    val label = stringResource(R.string.login_signing_in)

    Column(
        modifier = modifier
            .fillMaxWidth(LOGIN_FORM_WIDTH_FRACTION)
            .padding(24.dp)
            .semantics(mergeDescendants = true) { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkeletonBox(
            modifier = Modifier.size(LogoSize),
            shape = RoundedCornerShape(LogoCorner),
        )

        Spacer(Modifier.height(32.dp))
        SkeletonLine(widthFraction = 0.60f, height = 22.dp)
        Spacer(Modifier.height(10.dp))
        SkeletonLine(widthFraction = 0.42f, height = 13.dp)

        Spacer(Modifier.height(32.dp))
        FieldSkeleton()
        Spacer(Modifier.height(16.dp))
        FieldSkeleton()

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            SkeletonLine(modifier = Modifier.width(LinkWidth), height = LinkHeight)
        }

        Spacer(Modifier.height(20.dp))
        ButtonSkeleton()

        Spacer(Modifier.height(32.dp))
        SkeletonLine(widthFraction = 0.66f, height = LinkHeight)
    }
}

/** Mirrors [ChangePasswordForm] while the change-password request is in flight. */
@Composable
internal fun ChangePasswordSkeleton(modifier: Modifier = Modifier) {
    val label = stringResource(R.string.change_password_updating)

    Column(
        modifier = modifier
            .fillMaxWidth(CHANGE_PASSWORD_FORM_WIDTH_FRACTION)
            .padding(24.dp)
            .semantics(mergeDescendants = true) { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkeletonLine(widthFraction = 0.70f, height = 26.dp)
        Spacer(Modifier.height(32.dp))

        FieldSkeleton()
        Spacer(Modifier.height(16.dp))
        FieldSkeleton()
        Spacer(Modifier.height(16.dp))
        FieldSkeleton()

        Spacer(Modifier.height(32.dp))
        ButtonSkeleton()
    }
}

@Composable
private fun FieldSkeleton() {
    SkeletonBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(FieldHeight),
        shape = RoundedCornerShape(FieldCorner),
    )
}

@Composable
private fun ButtonSkeleton() {
    SkeletonBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight),
        shape = RoundedCornerShape(ButtonCorner),
    )
}

/** Kept in step with the fractions the real forms use, so the footprint does not shift. */
private const val LOGIN_FORM_WIDTH_FRACTION = 0.88f
private const val CHANGE_PASSWORD_FORM_WIDTH_FRACTION = 0.85f

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun LoginSkeletonPreview() {
    MaterialTheme {
        LoginSkeleton()
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun ChangePasswordSkeletonPreview() {
    MaterialTheme {
        ChangePasswordSkeleton()
    }
}
