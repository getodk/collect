package org.odk.collect.android.formhierarchy

import org.javarosa.core.model.FormIndex
import org.javarosa.form.api.FormEntryPrompt

sealed class HierarchyItem {
    abstract val formIndex: FormIndex
    abstract val primaryText: CharSequence
    abstract val id: Int

    data class Question(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
        val secondaryText: String,
        val formEntryPrompt: FormEntryPrompt
    ) : HierarchyItem() {
        override val id = ID

        companion object {
            const val ID = 0
        }
    }

    data class VisibleGroup(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
    ) : HierarchyItem() {
        override val id = ID

        companion object {
            const val ID = 1
        }
    }

    data class RepeatableGroup(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
    ) : HierarchyItem() {
        override val id = ID

        companion object {
            const val ID = 2
        }
    }

    data class RepeatInstance(
        override val formIndex: FormIndex,
        override val primaryText: CharSequence,
    ) : HierarchyItem() {
        override val id = ID

        companion object {
            const val ID = 3
        }
    }
}
