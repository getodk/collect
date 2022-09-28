package org.odk.collect.android.formentry.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.formentry.FormSessionRepository
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.shared.strings.UUIDGenerator

class InMemFormSessionRepository : FormSessionRepository {

    private val map = mutableMapOf<String, MutableLiveData<FormController?>>()

    override fun create(): String {
        return UUIDGenerator().generateUUID()
    }

    override fun get(id: String): LiveData<FormController?> {
        return getLiveData(id)
    }

    override fun set(id: String, formController: FormController) {
        getLiveData(id).value = formController
    }

    override fun clear(id: String) {
        getLiveData(id).value = null
    }

    private fun getLiveData(id: String): MutableLiveData<FormController?> {
        return map.getOrPut(id) { MutableLiveData<FormController?>(null) }
    }
}
