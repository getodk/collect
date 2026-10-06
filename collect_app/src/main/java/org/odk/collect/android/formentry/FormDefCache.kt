package org.odk.collect.android.formentry

import org.javarosa.core.model.FormDef
import java.io.File

interface FormDefCache {

    fun writeCache(formDef: FormDef?, formPath: String?)
    fun readCache(formXml: File?): FormDef?
}
