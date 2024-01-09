package org.odk.collect.externalapp

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ExternalAppUtilsTest {
    @Test
    fun `returnSingleValue should finish activity and pass expected value`() {
        val activity = mock<Activity>()
        val intent = Intent().also {
            it.putExtra("value", "blah")
        }

        ExternalAppUtils.returnSingleValue(activity, "blah")

        verify(activity).setResult(
            eq(Activity.RESULT_OK),
            argThat {
                intent.extras?.get("value") == "blah"
            }
        )
        verify(activity).finish()
    }

    @Test
    fun `getReturnIntent should return correct intent with string value`() {
        val intent = ExternalAppUtils.getReturnIntent("blah")

        assertThat(intent.extras?.get("value"), `is`("blah"))
    }

    @Test
    fun `getReturnedSingleValue should return correct integer value`() {
        val intent = Intent().apply {
            putExtra("value", 5)
        }

        assertThat(ExternalAppUtils.getReturnedSingleValue(intent), `is`(5))
    }

    @Test
    fun `getReturnedSingleValue should return correct decimal value`() {
        val intent = Intent().apply {
            putExtra("value", 5.5)
        }

        assertThat(ExternalAppUtils.getReturnedSingleValue(intent), `is`(5.5))
    }

    @Test
    fun `getReturnedSingleValue should return correct string value`() {
        val intent = Intent().apply {
            putExtra("value", "blah")
        }

        assertThat(ExternalAppUtils.getReturnedSingleValue(intent), `is`("blah"))
    }
}
