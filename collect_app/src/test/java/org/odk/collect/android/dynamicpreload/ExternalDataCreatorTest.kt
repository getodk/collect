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
class ExternalDataCreatorTest {

    @Test
    fun `does nothing if the form doesn't use dynamic preload`() {
        val form = FormDef().also {
            it.extras.put(DynamicPreloadExtra(false))
        }

        val mediaDir = TempFiles.createTempDir().also {
            File(it, "items.csv").writeText("name_key,name\nmango,Mango")
        }

        ExternalDataCreator().create(form, mediaDir, { false }, {})
        assertThat(mediaDir.listFiles().size, equalTo(1))
    }
}
