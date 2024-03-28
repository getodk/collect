package org.odk.collect.android.formentry

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.androidshared.data.getState
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.strings.UUIDGenerator

interface FormSessionRepository {
    fun create(): String
    fun get(id: String): LiveData<FormSession>
    fun clear(id: String)

    fun set(id: String, formController: FormController, form: Form) {
        set(id, formController, form, null)
    }

    fun set(id: String, formController: FormController, form: Form, instance: Instance?)

    fun update(id: String, instance: Instance?)
}

class AppStateFormSessionRepository(application: Application) : FormSessionRepository {

    private val appState = application.getState()

    override fun create(): String {
        return UUIDGenerator().generateUUID()
    }

    override fun get(id: String): LiveData<FormSession> {
        return getLiveData(id)
    }

    override fun set(id: String, formController: FormController, form: Form, instance: Instance?) {
        getLiveData(id).value = FormSession(formController, form, instance)
    }

    override fun update(id: String, instance: Instance?) {
        getLiveData(id).value?.let {
            getLiveData(id).value = FormSession(it.formController, it.form, instance)
        }
    }

    /**
     * Ensure the object gets completely removed. Simply nullifying it might cause memory leaks.
     * See: https://github.com/getodk/collect/issues/5777
     */
    override fun clear(id: String) {
        getLiveData(id).value = null
        appState.clear(getKey(id))
    }

    private fun getLiveData(id: String) =
        appState.get(getKey(id), MutableLiveData<FormSession>(null))

    private fun getKey(id: String) = "$KEY_PREFIX:$id"

    companion object {
        const val KEY_PREFIX = "formSession"
    }
}

data class FormSession @JvmOverloads constructor(
    val formController: FormController,
    val form: Form,
    val instance: Instance? = null
)
