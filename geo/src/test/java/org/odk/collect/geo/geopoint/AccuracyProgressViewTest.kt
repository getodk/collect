package org.odk.collect.geo.geopoint

import android.app.Application
import android.graphics.drawable.ColorDrawable
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.geo.R

@RunWith(AndroidJUnit4::class)
class AccuracyProgressViewTest {

    private val context = getApplicationContext<Application>().also {
        // Need a theme where primary and secondary are different for tests
        it.setTheme(com.google.android.material.R.style.Theme_MaterialComponents)
    }

    private val colorPrimary = getThemeAttributeValue(context, androidx.appcompat.R.attr.colorPrimary)
    private val colorOnPrimary = getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnPrimary)
    private val colorError = getThemeAttributeValue(context, androidx.appcompat.R.attr.colorError)
    private val colorOnError = getThemeAttributeValue(context, com.google.android.material.R.attr.colorOnError)

    @Test
    fun `updates current accuracy`() {
        val view = AccuracyProgressView(context)
        assertThat(
            view.binding.currentAccuracy.text,
            equalTo(context.getString(org.odk.collect.strings.R.string.empty_accuracy))
        )

        view.accuracy = LocationAccuracy.Improving(52f)
        assertThat(
            view.binding.currentAccuracy.text,
            equalTo(context.getString(org.odk.collect.strings.R.string.accuracy_m, "52"))
        )
    }

    @Test
    fun `updates text and strength based on accuracy`() {
        val view = AccuracyProgressView(context)

        assertThat(
            view.binding.text.text,
            equalTo(context.getString(org.odk.collect.strings.R.string.waiting_for_location))
        )
        assertThat(view.binding.strength.progress, equalTo(20))

        view.accuracy = LocationAccuracy.Unacceptable(10f)
        assertThat(
            view.binding.text.text,
            equalTo(context.getString(org.odk.collect.strings.R.string.unacceptable_accuracy))
        )
        assertThat(view.binding.strength.progress, equalTo(40))

        view.accuracy = LocationAccuracy.Poor(10f)
        assertThat(view.binding.text.text, equalTo(context.getString(org.odk.collect.strings.R.string.poor_accuracy)))
        assertThat(view.binding.strength.progress, equalTo(60))

        view.accuracy = LocationAccuracy.Improving(10f)
        assertThat(view.binding.text.text, equalTo(context.getString(org.odk.collect.strings.R.string.improving_accuracy)))
        assertThat(view.binding.strength.progress, equalTo(80))
    }

    @Test
    fun `has primary background when accuracy is poor`() {
        val view = AccuracyProgressView(context)
        view.accuracy = LocationAccuracy.Poor(10f)

        val backgroundColor = (view.binding.root.background as ColorDrawable).color
        assertThat(backgroundColor, equalTo(colorPrimary))

        val titleColor = view.binding.title.currentTextColor
        assertThat(titleColor, equalTo(colorOnPrimary))

        val currentAccuracyColor = view.binding.currentAccuracy.currentTextColor
        assertThat(currentAccuracyColor, equalTo(colorOnPrimary))

        val qualitativeColor = view.binding.text.currentTextColor
        assertThat(qualitativeColor, equalTo(colorOnPrimary))

        val strengthColor = view.binding.strength.indicatorColor[0]
        assertThat(strengthColor, equalTo(colorOnPrimary))
    }

    @Test
    fun `has error background when accuracy is unacceptable`() {
        val view = AccuracyProgressView(context)
        view.accuracy = LocationAccuracy.Unacceptable(10f)

        val backgroundColor = (view.binding.root.background as ColorDrawable).color
        assertThat(backgroundColor, equalTo(colorError))

        val titleColor = view.binding.title.currentTextColor
        assertThat(titleColor, equalTo(colorOnError))

        val currentAccuracyColor = view.binding.currentAccuracy.currentTextColor
        assertThat(currentAccuracyColor, equalTo(colorOnError))

        val qualitativeColor = view.binding.text.currentTextColor
        assertThat(qualitativeColor, equalTo(colorOnError))

        val strengthColor = view.binding.strength.indicatorColor[0]
        assertThat(strengthColor, equalTo(colorOnError))
    }
}
