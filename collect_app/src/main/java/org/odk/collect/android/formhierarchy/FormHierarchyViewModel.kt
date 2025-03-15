package org.odk.collect.android.formhierarchy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormHierarchyViewModel() as T
        }
    }
}
