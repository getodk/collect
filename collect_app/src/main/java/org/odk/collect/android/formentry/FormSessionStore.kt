package org.odk.collect.android.formentry

import org.odk.collect.android.javarosawrapper.FormController

object FormSessionStore {

    private val map = mutableMapOf<String, FormController>()

    @JvmStatic
    fun get(id: String): FormController? {
        return map[id]
    }

    @JvmStatic
    fun set(id: String, formController: FormController) {
        map[id] = formController
    }
}
