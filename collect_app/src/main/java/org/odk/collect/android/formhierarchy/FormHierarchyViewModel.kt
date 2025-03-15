package org.odk.collect.android.formhierarchy

import androidx.lifecycle.ViewModel
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.instance.TreeReference

class FormHierarchyViewModel : ViewModel() {
    var contextGroupRef: TreeReference? = null
    var screenIndex: FormIndex? = null
    var repeatGroupPickerIndex: FormIndex? = null
    var currentIndex: FormIndex? = null
    var elementsToDisplay: List<HierarchyItem>? = null
    var startIndex: FormIndex? = null

    fun shouldShowRepeatGroupPicker() = repeatGroupPickerIndex != null
}
