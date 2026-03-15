package org.odk.collect.timedgrid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

interface TimedGridRenderer {
    fun inflateView(inflater: LayoutInflater, parent: ViewGroup): View

    fun bind(
        root: View,
        configuration: TimedGridWidgetConfiguration,
        items: List<GridItem>,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onFinish: () -> Unit,
        onTogglePause: () -> Unit
    )

    fun updateUIForState(state: TimedGridState)

    fun updateTimer(timeString: String)

    fun getLastSelectedLastItemValue(): String?

    fun getToggledAnswers(): Set<String>

    fun firstLineAllIncorrect(): Boolean

    fun restoreAnswers(toggled: Set<String>, last: String?) {}
}
