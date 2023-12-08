package org.odk.collect.android.dynamicpreload

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormDef
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.shared.TempFiles
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExternalDataUseCasesTest {

    @Test
    fun `create() does nothing if the form doesn't use dynamic preload`() {
        val form = FormDef().also {
            it.extras.put(DynamicPreloadExtra(false))
        }

        val mediaDir = TempFiles.createTempDir().also {
            File(it, "items.csv").writeText("name_key,name\nmango,Mango")
        }

        ExternalDataUseCases.create(form, mediaDir, { false }, {})
        assertThat(mediaDir.listFiles().size, equalTo(1))
    }

    @Test
    fun `create() works if the FormDef does not have a DynamicPreloadExtra()`() {
        val form = FormDef()
        val mediaDir = TempFiles.createTempDir().also {
            File(it, "items.csv").writeText("name_key,name\nmango,Mango")
        }

        ExternalDataUseCases.create(form, mediaDir, { false }, {})
        assertThat(mediaDir.listFiles().size, equalTo(2))
    }

    @Test
    fun `create() leaves original CSV in place`() {
        val form = FormDef().also {
            it.extras.put(DynamicPreloadExtra(true))
        }

        val mediaDir = TempFiles.createTempDir()
        val csv = File(mediaDir, "items.csv").also { it.writeText("name_key,name\nmango,Mango") }

        ExternalDataUseCases.create(form, mediaDir, { false }, {})
        assertThat(csv.exists(), equalTo(true))
    }
}
