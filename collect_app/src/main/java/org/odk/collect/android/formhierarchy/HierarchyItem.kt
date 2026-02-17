package org.odk.collect.android.formhierarchy

import org.javarosa.core.model.FormIndex
import org.javarosa.form.api.FormEntryPrompt

sealed class HierarchyItem {
    abstract val formIndex: FormIndex
    abstract val primaryText: CharSequence

    data class Question(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
        val secondaryText: String,
        val formEntryPrompt: FormEntryPrompt
    ) : HierarchyItem()

    data class VisibleGroup(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
    ) : HierarchyItem()

    data class RepeatableGroup(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
    ) : HierarchyItem()

    data class RepeatInstance(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
    ) : HierarchyItem()
}
