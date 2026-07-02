package org.odk.collect.androidshared.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView

/**
 * Requests a layout pass on the host [android.view.View] after every composition.
 *
 * A [androidx.compose.ui.platform.ComposeView] reused from a RecyclerView pool can get measured to
 * 0 height on rebind, so its row renders blank after scrolling. Wrapping the content in this
 * Composable nudges a remeasure once composition completes, applying the real height.
 *
 * This function is `inline` so that the SideEffect remains part of the caller's
 * composition. Without `inline`, Compose may skip this wrapper during recomposition,
 * preventing the SideEffect from running.
 */
@Composable
inline fun RemeasureHostOnCompose(content: @Composable () -> Unit) {
    val hostView = LocalView.current
    SideEffect {
        hostView.requestLayout()
    }

    content()
}
