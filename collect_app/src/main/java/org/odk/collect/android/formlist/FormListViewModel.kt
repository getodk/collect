package org.odk.collect.android.formlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.data.Consumable
import org.odk.collect.forms.FormsRepository

class FormListViewModel(private val formsRepository: FormsRepository) : ViewModel() {
    private val _forms: MutableLiveData<Consumable<List<FormListItem>>> = MutableLiveData()
    val forms: LiveData<Consumable<List<FormListItem>>> = _forms

    fun fetchForms() {
        _forms.value = Consumable(
            formsRepository
                .all
                .map { form ->
                    FormListItem(
                        formId = form.dbId,
                        formName = form.displayName,
                        formVersion = form.version ?: "",
                        geometryPath = form.geometryXpath ?: "",
                        dateOfCreation = form.date,
                        dateOfLastUsage = 0
                    )
                }
                .toList()
        )
    }

    class Factory(private val formsRepository: FormsRepository) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormListViewModel(formsRepository) as T
        }
    }
}
