package org.odk.collect.material

import android.app.Application
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class ErrorsPillTest {
    private val context = ApplicationProvider.getApplicationContext<Application>().also {
        it.setTheme(com.google.android.material.R.style.Theme_Material3_Light)
    }
    private val attrs = Robolectric.buildAttributeSet().build()
    private val errorsPill: ErrorsPill = ErrorsPill(context, attrs)

    @Test
    fun `setup with State ERRORS should set appropriate properties`() {
        errorsPill.errors = true
        assertErrorsPill()
    }

    @Test
    fun `setup with State NO_ERRORS should set appropriate properties`() {
        errorsPill.errors = false
        assertNoErrorsPill()
    }

    @Test
    fun `pill can be recycled`() {
        errorsPill.errors = true
        assertErrorsPill()

        errorsPill.errors = false
        assertNoErrorsPill()
    }

    private fun assertErrorsPill() {
        assertThat(errorsPill.visibility, equalTo(View.VISIBLE))
        assertThat(Shadows.shadowOf(errorsPill.binding.icon.drawable).createdFromResId, equalTo(org.odk.collect.icons.R.drawable.ic_baseline_rule_24))
        assertThat(errorsPill.binding.text.text, equalTo(context.getString(org.odk.collect.strings.R.string.draft_errors)))
    }

    private fun assertNoErrorsPill() {
        assertThat(errorsPill.visibility, equalTo(View.VISIBLE))
        assertThat(Shadows.shadowOf(errorsPill.binding.icon.drawable).createdFromResId, equalTo(org.odk.collect.icons.R.drawable.ic_baseline_check_24))
        assertThat(errorsPill.binding.text.text, equalTo(context.getString(org.odk.collect.strings.R.string.draft_no_errors)))
    }
}
