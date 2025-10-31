package org.odk.collect.androidtest

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider

fun SemanticsNodeInteractionsProvider.onNodeWithClickLabel(
    label: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
    useUnmergedTree: Boolean = false,
): SemanticsNodeInteraction =
    onNode(hasClickLabel(label, substring, ignoreCase), useUnmergedTree)

fun hasClickLabel(
    label: String,
    substring: Boolean = false,
    ignoreCase: Boolean = false,
): SemanticsMatcher {
    return if (substring) {
        SemanticsMatcher("Clickable action label contains '$label' (ignoreCase: $ignoreCase)") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label
                ?.contains(label, ignoreCase) == true
        }
    } else {
        SemanticsMatcher("Clickable action label = '$label' (ignoreCase: $ignoreCase)") {
            it.config.getOrNull(SemanticsActions.OnClick)?.label
                ?.equals(label, ignoreCase) == true
        }
    }
}
