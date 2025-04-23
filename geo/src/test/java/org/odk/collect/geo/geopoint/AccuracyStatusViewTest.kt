package org.odk.collect.geo.geopoint

import android.app.Application
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.strings.R

@RunWith(AndroidJUnit4::class)
class AccuracyStatusViewTest {

    private val application = ApplicationProvider.getApplicationContext<Application>().also {
        it.setTheme(com.google.android.material.R.style.Theme_MaterialComponents)
    }

    @Test
    fun `hides title when text is blank`() {
        val view = AccuracyStatusView(application)
        assertThat(view.binding.title.visibility, equalTo(View.GONE))
    }

    @Test
    fun `shows title when text is not blank`() {
        val view = AccuracyStatusView(application)
        view.title = "SOMETHING!"
        assertThat(view.binding.title.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun `shows warning when accuracy is Unacceptable`() {
        val view = AccuracyStatusView(application)

        view.accuracy = LocationAccuracy.Unacceptable(0.0f)
        assertThat(
            view.binding.locationStatus.text,
            equalTo(application.getString(R.string.location_accuracy_unacceptable, "0 m"))
        )

        view.accuracy = LocationAccuracy.Improving(0.0f)
        assertThat(
            view.binding.locationStatus.text,
            equalTo(application.getString(R.string.location_accuracy, "0 m"))
        )
    }
}
