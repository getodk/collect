package org.odk.collect.android.formmanagement

import org.junit.Assert
import org.junit.Test
import org.odk.collect.android.formmanagement.LocalFormUseCases.filterFormsToAdd
import java.io.File

class FilterFormsToAddTest {

    @Test
    fun filterEmptyListOfForms() {
        val formDefs = arrayOf<File>()
        val files: List<File?> = filterFormsToAdd(formDefs, 0)
        Assert.assertEquals(0, files.size.toLong())
    }

    @Test
    fun filterNullListOfForms() {
        val files: List<File?> = filterFormsToAdd(null, 0)
        Assert.assertEquals(0, files.size.toLong())
    }
}
