package org.odk.collect.android.formentry

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R

@RunWith(AndroidJUnit4::class)
class FormEndViewTest {

    @Test
    fun `focusing on save as field shows warning`() {
        val context = ApplicationProvider.getApplicationContext<Context>().also {
            it.setTheme(R.style.Theme_Material3_Light)
        }

        val formEndView = FormEndView(
            context,
            null,
            null,
            false,
            null
        )

        val warningView = formEndView.findViewById<View>(R.id.manual_name_warning)
        assertThat(warningView.visibility, equalTo(View.GONE))

        formEndView.findViewById<View>(R.id.save_name).requestFocus()
        assertThat(warningView.visibility, equalTo(View.VISIBLE))
    }
}
