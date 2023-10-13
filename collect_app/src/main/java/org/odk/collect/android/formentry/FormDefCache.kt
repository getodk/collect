package org.odk.collect.android.formentry

import org.javarosa.core.model.FormDef
import java.io.File
import java.io.IOException

interface FormDefCache {

    @Throws(IOException::class)
    fun writeCache(formDef: FormDef?, formPath: String?)
    fun readCache(formXml: File?): FormDef?
}
