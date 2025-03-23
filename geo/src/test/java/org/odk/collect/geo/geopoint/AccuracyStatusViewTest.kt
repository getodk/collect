package org.odk.collect.geo.geopoint

import android.app.Application
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue

@RunWith(AndroidJUnit4::class)
class AccuracyStatusViewTest {

    private val application = ApplicationProvider.getApplicationContext<Application>().also {
        it.setTheme(com.google.android.material.R.style.Theme_MaterialComponents)
    }

    private val colorError =
        getThemeAttributeValue(application, com.google.android.material.R.attr.colorError)
    private val colorOnError =
        getThemeAttributeValue(application, com.google.android.material.R.attr.colorOnError)
    private val colorSurface =
        getThemeAttributeValue(application, com.google.android.material.R.attr.colorSurface)
    private val colorOnSurface =
        getThemeAttributeValue(application, com.google.android.material.R.attr.colorOnSurface)

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
    fun `uses error background color when accuracy is Unacceptable`() {
        val view = AccuracyStatusView(application)

        view.accuracy = LocationAccuracy.Unacceptable(0.0f)
        assertThat((view.background as ColorDrawable).color, equalTo(colorError))
        assertThat(view.binding.title.currentTextColor, equalTo(colorOnError))
        assertThat(view.binding.locationStatus.currentTextColor, equalTo(colorOnError))

        view.accuracy = LocationAccuracy.Improving(0.0f)
        assertThat((view.background as ColorDrawable).color, equalTo(colorSurface))
        assertThat(view.binding.title.currentTextColor, equalTo(colorOnSurface))
        assertThat(view.binding.locationStatus.currentTextColor, equalTo(colorOnSurface))
    }
}
