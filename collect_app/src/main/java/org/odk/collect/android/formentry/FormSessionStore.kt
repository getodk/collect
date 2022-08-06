package org.odk.collect.android.formentry

import android.app.Application
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.androidshared.data.getState

interface FormSessionStore {
    fun get(id: String): FormController?
    fun set(id: String, formController: FormController)
}

class InMemFormSessionStore : FormSessionStore {

    private val map = mutableMapOf<String, FormController>()

    override fun get(id: String): FormController? {
        return map[id]
    }

    override fun set(id: String, formController: FormController) {
        map[id] = formController
    }
}

class AppStateFormSessionStore(application: Application) : FormSessionStore {

    private val appState = application.getState()

    override fun get(id: String): FormController? {
        return appState.get(getKey(id))
    }

    override fun set(id: String, formController: FormController) {
        appState.set(getKey(id), formController)
    }

    private fun getKey(id: String) = "$KEY_PREFIX:$id"

    companion object {
        const val KEY_PREFIX = "formSession"
    }
}
