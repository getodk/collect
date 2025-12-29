package org.odk.collect.android.formhierarchy

import org.javarosa.core.model.FormIndex
import org.javarosa.form.api.FormEntryPrompt

data class HierarchyItem @JvmOverloads constructor(
    val formIndex: FormIndex,
    val hierarchyItemType: HierarchyItemType,
    val primaryText: CharSequence,
    val answer: String? = null,
    val formEntryPrompt: FormEntryPrompt? = null
)

enum class HierarchyItemType(val id: Int) {
    QUESTION(0), VISIBLE_GROUP(1), REPEATABLE_GROUP(2), REPEAT_INSTANCE(3)
}
