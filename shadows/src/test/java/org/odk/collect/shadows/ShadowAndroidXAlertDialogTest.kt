package org.odk.collect.shadows

import android.view.View
import androidx.appcompat.app.AlertDialog
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.extract

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowAndroidXAlertDialog::class])
class ShadowAndroidXAlertDialogTest {

    @Test
    fun `getView returns view set on builder`() {
        val context = RuntimeEnvironment.getApplication().also {
            it.setTheme(R.style.Theme_AppCompat)
        }

        val view = View(context)
        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .show()

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        assertThat(shadowDialog.getView(), equalTo(view))
    }
}
