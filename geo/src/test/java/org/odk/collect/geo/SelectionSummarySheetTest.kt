package org.odk.collect.geo

import android.app.Application
import android.view.View
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.geo.support.Fixtures
import org.odk.collect.testshared.RobolectricHelpers.getCreatedFromResId

@RunWith(AndroidJUnit4::class)
class SelectionSummarySheetTest {

    private val application = getApplicationContext<Application>().also {
        it.setTheme(R.style.Theme_MaterialComponents)
    }

    @Test
    fun `setItem shows name`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectItem().copy(
                name = "Cosmic Dread"
            )
        )

        assertThat(selectionSummarySheet.binding.name.text, equalTo("Cosmic Dread"))
    }

    @Test
    fun `setItem shows status`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectItem().copy(
                status = MappableSelectItem.IconifiedText(
                    android.R.drawable.ic_btn_speak_now,
                    "Emotion"
                )
            )
        )

        assertThat(selectionSummarySheet.binding.statusText.text, equalTo("Emotion"))
        val iconDrawable = selectionSummarySheet.binding.statusIcon.drawable
        assertThat(getCreatedFromResId(iconDrawable), equalTo(android.R.drawable.ic_btn_speak_now))
    }

    @Test
    fun `setItem shows info and hides action when it is non-null`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.infoMappableSelectItem().copy(
                info = "Don't even bother looking"
            )
        )

        assertThat(selectionSummarySheet.binding.info.visibility, equalTo(View.VISIBLE))
        assertThat(selectionSummarySheet.binding.action.visibility, equalTo(View.GONE))
        assertThat(selectionSummarySheet.binding.info.text, equalTo("Don't even bother looking"))
    }

    @Test
    fun `setItem shows action and hides info when it is non-null`() {
        val selectionSummarySheet = SelectionSummarySheet(application)
        selectionSummarySheet.setItem(
            Fixtures.actionMappableSelectItem().copy(
                action = MappableSelectItem.IconifiedText(
                    android.R.drawable.ic_btn_speak_now,
                    "Come on in"
                )
            )
        )

        assertThat(selectionSummarySheet.binding.action.visibility, equalTo(View.VISIBLE))
        assertThat(selectionSummarySheet.binding.info.visibility, equalTo(View.GONE))

        assertThat(selectionSummarySheet.binding.action.text, equalTo("Come on in"))
        val iconDrawable = selectionSummarySheet.binding.action.chipIcon
        assertThat(
            getCreatedFromResId(iconDrawable!!),
            equalTo(android.R.drawable.ic_btn_speak_now)
        )
    }
}
