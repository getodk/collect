package org.odk.collect.android.formhierarchy

import android.graphics.drawable.Drawable
import org.javarosa.core.model.FormIndex
import org.odk.collect.android.formhierarchy.HierarchyElement.Type

/**
 * Represents a question or repeat to be shown in
 * [FormHierarchyActivity].
 */
class HierarchyElement(
    /**
     * The primary text this element should be displayed with.
     */
    val primaryText: CharSequence,
    /**
     * The secondary text this element should be displayed with.
     */
    val secondaryText: String?,
    /**
     * An optional icon.
     */
    var icon: Drawable?,
    /**
     * The type and state of this element. See [Type].
     */
    val type: Type,
    /**
     * The form index of this element.
     */
    val formIndex: FormIndex
) {

    /**
     * The type and state of this element.
     */
    enum class Type {
        QUESTION, VISIBLE_GROUP, REPEATABLE_GROUP, REPEAT_INSTANCE
    }
}
