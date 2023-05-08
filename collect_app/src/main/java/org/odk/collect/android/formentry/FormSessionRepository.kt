package org.odk.collect.android.formentry

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.androidshared.data.getState
import org.odk.collect.forms.Form
import org.odk.collect.shared.strings.UUIDGenerator

interface FormSessionRepository {
    fun create(): String
    fun get(id: String): LiveData<FormSession>
    fun set(id: String, formController: FormController, form: Form)
    fun clear(id: String)
}

class AppStateFormSessionRepository(application: Application) : FormSessionRepository {

    private val appState = application.getState()

    override fun create(): String {
        return UUIDGenerator().generateUUID()
    }

    override fun get(id: String): LiveData<FormSession> {
        return getLiveData(id)
    }

    override fun set(id: String, formController: FormController, form: Form) {
        getLiveData(id).value = FormSession(formController, form)
    }

    override fun clear(id: String) {
        getLiveData(id).value = null
    }

    private fun getLiveData(id: String) =
        appState.get(getKey(id), MutableLiveData<FormSession>(null))

    private fun getKey(id: String) = "$KEY_PREFIX:$id"

    companion object {
        const val KEY_PREFIX = "formSession"
    }
}

data class FormSession(
    val formController: FormController,
    val form: Form
)
