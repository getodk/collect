package org.odk.collect.android.formentry

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class FormEndViewTest {

    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Material3_Light)
        }

    @Test
    fun `focusing on save as field shows warning`() {
        val formEndView = FormEndView(
            context,
            null,
            null,
            null
        )

        val warningView = formEndView.findViewById<View>(R.id.manual_name_warning)
        assertThat(warningView.visibility, equalTo(View.GONE))

        formEndView.findViewById<View>(R.id.save_name).requestFocus()
        assertThat(warningView.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun `clicking learn more in warning opens forum post`() {
        val formEndView = FormEndView(
            context,
            null,
            null,
            null
        )

        formEndView.findViewById<View>(R.id.save_name).requestFocus()
        formEndView.findViewById<View>(R.id.instance_name_learn_more).performClick()

        val intent = shadowOf(context).nextStartedActivity
        assertThat(intent.action, equalTo(Intent.ACTION_VIEW))
        assertThat(intent.flags, equalTo(Intent.FLAG_ACTIVITY_NEW_TASK))
        assertThat(
            intent.data,
            equalTo(Uri.parse("https://forum.getodk.org/t/collect-manual-instance-naming-will-be-removed-in-v2023-2/40313"))
        )
    }
}
