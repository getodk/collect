package org.odk.collect.android.instancemanagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.strings.R
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class InstanceExtKtTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun getStatusDescriptionTest() {
        val incomplete = InstanceFixtures.instance(status = Instance.STATUS_INCOMPLETE)
        assertDateFormat(
            incomplete.getStatusDescription(resources),
            R.string.saved_on_date_at_time
        )

        val invalid = InstanceFixtures.instance(status = Instance.STATUS_INVALID)
        assertDateFormat(
            invalid.getStatusDescription(resources),
            R.string.saved_on_date_at_time
        )

        val valid = InstanceFixtures.instance(status = Instance.STATUS_VALID)
        assertDateFormat(
            valid.getStatusDescription(resources),
            R.string.saved_on_date_at_time
        )

        val complete = InstanceFixtures.instance(status = Instance.STATUS_COMPLETE)
        assertDateFormat(
            complete.getStatusDescription(resources),
            R.string.finalized_on_date_at_time
        )

        val submitted = InstanceFixtures.instance(status = Instance.STATUS_SUBMITTED)
        assertDateFormat(
            submitted.getStatusDescription(resources),
            R.string.sent_on_date_at_time
        )

        val submissionFailed = InstanceFixtures.instance(status = Instance.STATUS_SUBMISSION_FAILED)
        assertDateFormat(
            submissionFailed.getStatusDescription(resources),
            R.string.sending_failed_on_date_at_time
        )
    }

    private fun assertDateFormat(description: String, stringId: Int) {
        assertThat(
            description,
            equalTo(
                SimpleDateFormat(
                    resources.getString(stringId),
                    Locale.getDefault()
                ).format(0)
            )
        )
    }
}
