package org.odk.collect.geo

import android.app.Application
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue

@RunWith(AndroidJUnit4::class)
class AccuracyStatusViewTest {

    private val context = getApplicationContext<Application>().also {
        it.setTheme(R.style.Theme_MaterialComponents)
    }

    private val colorPrimary = getThemeAttributeValue(context, R.attr.colorPrimary)
    private val colorOnPrimary = getThemeAttributeValue(context, R.attr.colorOnPrimary)
    private val colorError = getThemeAttributeValue(context, R.attr.colorError)
    private val colorOnError = getThemeAttributeValue(context, R.attr.colorOnError)

    @Test
    fun `initially hides info and shows progress bar`() {
        val view = AccuracyStatusView(context)

        assertThat(view.binding.progressBar.visibility, equalTo(View.VISIBLE))
        assertThat(view.binding.currentAccuracy.visibility, equalTo(View.GONE))
        assertThat(view.binding.qualitative.visibility, equalTo(View.GONE))
        assertThat(view.binding.action.visibility, equalTo(View.GONE))
    }

    @Test
    fun `hides progress bar and shows info when setAccuracy is called`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(9.0f, 5.0f)

        assertThat(view.binding.progressBar.visibility, equalTo(View.GONE))
        assertThat(view.binding.currentAccuracy.visibility, equalTo(View.VISIBLE))
        assertThat(view.binding.qualitative.visibility, equalTo(View.VISIBLE))
        assertThat(view.binding.action.visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun `shows distance from threshold when accuracy is less than 10m`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(9.0f, 5.0f)

        assertThat(
            view.binding.qualitative.text,
            equalTo(context.getString(R.string.distance_from_accuracy_goal, "4m", "5m"))
        )
    }

    @Test
    fun `has primary background when accuracy is less than 100m`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(99.0f, 5.0f)

        val backgroundColor = (view.binding.root.background as ColorDrawable).color
        assertThat(backgroundColor, equalTo(colorPrimary))

        val titleColor = view.binding.title.currentTextColor
        assertThat(titleColor, equalTo(colorOnPrimary))

        val currentAccuracyColor = view.binding.currentAccuracy.currentTextColor
        assertThat(currentAccuracyColor, equalTo(colorOnPrimary))

        val qualitativeColor = view.binding.qualitative.currentTextColor
        assertThat(qualitativeColor, equalTo(colorOnPrimary))

        val actionColor = view.binding.action.currentTextColor
        assertThat(actionColor, equalTo(colorOnPrimary))
    }

    @Test
    fun `shows accuracy as poor when accuracy is less than 100m`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(99.0f, 5.0f)

        assertThat(
            view.binding.qualitative.text,
            equalTo(context.getString(R.string.poor_accuracy))
        )
    }

    @Test
    fun `shows prompt to wait when accuracy is less than 100m`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(99.0f, 5.0f)

        assertThat(
            view.binding.action.text,
            equalTo(context.getString(R.string.please_wait_for_improved_accuracy))
        )
    }

    @Test
    fun `has error background when accuracy is 100m or greater`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(100.0f, 5.0f)

        val backgroundColor = (view.binding.root.background as ColorDrawable).color
        assertThat(backgroundColor, equalTo(colorError))

        val titleColor = view.binding.title.currentTextColor
        assertThat(titleColor, equalTo(colorOnError))

        val currentAccuracyColor = view.binding.currentAccuracy.currentTextColor
        assertThat(currentAccuracyColor, equalTo(colorOnError))

        val qualitativeColor = view.binding.qualitative.currentTextColor
        assertThat(qualitativeColor, equalTo(colorOnError))

        val actionColor = view.binding.action.currentTextColor
        assertThat(actionColor, equalTo(colorOnError))
    }

    @Test
    fun `shows accuracy as unnaceptable when accuracy is 100m or greater`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(100.0f, 5.0f)

        assertThat(
            view.binding.qualitative.text,
            equalTo(context.getString(R.string.unacceptable_accuracy))
        )
    }

    @Test
    fun `shows prompt to see the sky when accuracy is 100m or greater`() {
        val view = AccuracyStatusView(context)
        view.setAccuracy(100.0f, 5.0f)

        assertThat(
            view.binding.action.text,
            equalTo(context.getString(R.string.unacceptable_accuracy_tip))
        )
    }
}
