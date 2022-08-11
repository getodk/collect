package org.odk.collect.android.formentry

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.androidshared.data.getState

interface FormSessionStore {
    fun get(id: String): LiveData<FormController?>
    fun set(id: String, formController: FormController)
}

class InMemFormSessionStore : FormSessionStore {

    private val map = mutableMapOf<String, MutableLiveData<FormController?>>()

    override fun get(id: String): LiveData<FormController?> {
        return getLiveData(id)
    }

    override fun set(id: String, formController: FormController) {
        getLiveData(id).value = formController
    }

    private fun getLiveData(id: String): MutableLiveData<FormController?> {
        return map.getOrPut(id) { MutableLiveData<FormController?>(null) }
    }
}

class AppStateFormSessionStore(application: Application) : FormSessionStore {

    private val appState = application.getState()

    override fun get(id: String): LiveData<FormController?> {
        return getLiveData(id)
    }

    override fun set(id: String, formController: FormController) {
        getLiveData(id).value = formController
    }

    private fun getLiveData(id: String) =
        appState.get(getKey(id), MutableLiveData<FormController?>(null))

    private fun getKey(id: String) = "$KEY_PREFIX:$id"

    companion object {
        const val KEY_PREFIX = "formSession"
    }
}
