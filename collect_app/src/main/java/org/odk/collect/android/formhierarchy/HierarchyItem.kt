package org.odk.collect.android.formhierarchy

import org.javarosa.core.model.FormIndex

data class HierarchyItem(
    val formIndex: FormIndex,
    val hierarchyItemType: HierarchyItemType,
    val primaryText: CharSequence,
    val secondaryText: String?
) {
    constructor(
        formIndex: FormIndex,
        hierarchyItemType: HierarchyItemType,
        primaryText: CharSequence
    ) : this(formIndex, hierarchyItemType, primaryText, null)
}

enum class HierarchyItemType(val id: Int) {
    QUESTION(0), VISIBLE_GROUP(1), REPEATABLE_GROUP(2), REPEAT_INSTANCE(3)
}
