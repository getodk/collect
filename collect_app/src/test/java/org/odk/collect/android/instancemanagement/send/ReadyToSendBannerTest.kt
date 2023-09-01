package org.odk.collect.android.instancemanagement.send

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
import org.odk.collect.shared.TimeInMs

@RunWith(AndroidJUnit4::class)
class ReadyToSendBannerTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Collect)
        }

    @Test
    fun `if there are no sent instances and no instances ready to send do not display the banner`() {
        val data = ReadyToSendViewModel.Data(0, 0, 0)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are sent instances but no instances ready to send do not display the banner`() {
        val data = ReadyToSendViewModel.Data(0, 1, 0)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are instances ready to send but no sent instances do not display the banner`() {
        val data = ReadyToSendViewModel.Data(1, 0, 0)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(view.findViewById<ConstraintLayout>(R.id.banner).visibility, equalTo(View.GONE))
    }

    @Test
    fun `if there are both sent and ready to send instances display the banner`() {
        val data = ReadyToSendViewModel.Data(1, 1, 0)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(
            view.findViewById<ConstraintLayout>(R.id.banner).visibility,
            equalTo(View.VISIBLE)
        )
    }

    @Test
    fun `the banner should display how long ago in seconds the last instance was sent if it was less than a minute ago`() {
        val data = ReadyToSendViewModel.Data(1, 1, TimeInMs.ONE_SECOND * 5)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 5 seconds ago")
        )
    }

    @Test
    fun `the banner should display how long ago in minutes the last instance was sent if it was less than an hour ago`() {
        val data = ReadyToSendViewModel.Data(1, 1, TimeInMs.ONE_MINUTE)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 1 minute ago")
        )
    }

    @Test
    fun `the banner should display how long ago in hours the last instance was sent if it was less than a day ago`() {
        val data = ReadyToSendViewModel.Data(1, 1, TimeInMs.ONE_HOUR * 2)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 2 hours ago")
        )
    }

    @Test
    fun `the banner should display how long ago in days the last instance was sent if it was more than 24 hours ago`() {
        val data = ReadyToSendViewModel.Data(1, 1, TimeInMs.ONE_DAY * 34)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.title).text,
            equalTo("Last form sent: 34 days ago")
        )
    }

    @Test
    fun `the banner should display the number of instances ready to send`() {
        val data = ReadyToSendViewModel.Data(3, 1, 0)
        val view = ReadyToSendBanner(context).also {
            it.setData(data)
        }

        assertThat(
            view.findViewById<MaterialTextView>(R.id.subtext).text,
            equalTo("3 forms ready to send")
        )
    }
}
