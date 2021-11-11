package org.odk.collect.android.adapters

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.logic.HierarchyElement

@RunWith(AndroidJUnit4::class)
class HierarchyListItemViewTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun whenSecondaryTextIsHtml_displaysAsIs() {
        val view = HierarchyListItemView(context)

        view.setElement(HierarchyElement("Blah", "<h1></h1>", null, HierarchyElement.Type.QUESTION, mock()))
        assertThat(view.binding.secondaryText.text, `is`("<h1></h1>"))
    }
}
