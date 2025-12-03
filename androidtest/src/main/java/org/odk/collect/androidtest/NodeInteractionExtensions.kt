package org.odk.collect.androidtest

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.test.core.app.ApplicationProvider

fun SemanticsNodeInteractionsProvider.onNodeWithClickLabel(@StringRes labelRes: Int): SemanticsNodeInteraction {
    val context = ApplicationProvider.getApplicationContext<Application>()
    return onNodeWithClickLabel(context.getString(labelRes))
}

fun SemanticsNodeInteractionsProvider.onNodeWithClickLabel(label: String): SemanticsNodeInteraction =
    onNode(hasClickLabel(label))

fun hasClickLabel(label: String): SemanticsMatcher {
    return SemanticsMatcher("Clickable action label = '$label'") {
        it.config.getOrNull(SemanticsActions.OnClick)?.label?.equals(label) == true
    }
}
