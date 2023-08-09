package org.odk.collect.android.readytosend

import android.app.Application
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository

@RunWith(AndroidJUnit4::class)
class ReadyToSendBannerTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Collect)
        }

    private val instancesRepository = InMemInstancesRepository().also {
        it.save(
            Instance.Builder()
                .formId("1")
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )
    }

    @Test
    fun `if there are no sent instances do not display the banner`() {
        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are no instances ready to send do not display the banner`() {
        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are sent instances but no instances ready to send do not display the banner`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are instances ready to send (complete) but no sent instances do not display the banner`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are instances ready to send (submission failed) but no sent instances do not display the banner`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are both sent and ready to send instances display the banner`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(
            view.findViewById<ConstraintLayout>(R.id.banner).visibility,
            equalTo(View.VISIBLE)
        )
    }

    @Test
    fun `the banner should display how long ago in seconds the last instance was sent if it was less than a minute ago`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(0)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { ONE_SECOND * 5 }
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 5 seconds ago")
        )
    }

    @Test
    fun `the banner should display how long ago in minutes the last instance was sent if it was less than an hour ago`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(0)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { ONE_MINUTE }
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 1 minute ago")
        )
    }

    @Test
    fun `the banner should display how long ago in hours the last instance was sent if it was less than a day ago`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(0)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { ONE_HOUR * 2 }
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 2 hours ago")
        )
    }

    @Test
    fun `the banner should display how long ago in days the last instance was sent if it was more than 24 hours ago`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(0)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { ONE_DAY * 34 }
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 34 days ago")
        )
    }

    @Test
    fun `the banner should display how long ago the last instance was sent if there are multiple sent instances`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(0)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("4")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(ONE_SECOND * 5)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("5")
                .status(Instance.STATUS_SUBMITTED)
                .lastStatusChangeDate(ONE_SECOND * 4)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { ONE_SECOND * 10 }
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 5 seconds ago")
        )
    }

    @Test
    fun `the banner should display the number of instances ready to send`() {
        instancesRepository.save(
            Instance.Builder()
                .formId("2")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("3")
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("4")
                .status(Instance.STATUS_COMPLETE)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("5")
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        val view = ReadyToSendBanner(context).also {
            it.init(instancesRepository) { 0 }
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.subtext).text,
            equalTo("3 forms ready to send")
        )
    }
}
