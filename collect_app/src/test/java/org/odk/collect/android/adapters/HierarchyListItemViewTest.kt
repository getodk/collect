package org.odk.collect.android.adapters

import android.app.Application
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.logic.HierarchyElement

@RunWith(AndroidJUnit4::class)
class HierarchyListItemViewTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `When icon is not specified should be gone`() {
        val view = HierarchyListItemView(context)

        view.setElement(getHierarchyElement(null, "", ""))

        assertThat(view.binding.icon.visibility, `is`(View.GONE))
    }

    @Test
    fun `When icon is specified should be visible`() {
        val view = HierarchyListItemView(context)

        val icon = ContextCompat.getDrawable(context, R.drawable.ic_folder_open)
        view.setElement(getHierarchyElement(icon, "", ""))

        assertThat(view.binding.icon.drawable, `is`(icon))
        assertThat(view.binding.icon.visibility, `is`(View.VISIBLE))
    }

    @Test
    fun `Primary text should be visible`() {
        val view = HierarchyListItemView(context)

        view.setElement(getHierarchyElement(null, "Primary text", ""))

        assertThat(view.binding.primaryText.visibility, `is`(View.VISIBLE))
        assertThat(view.binding.primaryText.text.toString(), `is`("Primary text"))
    }

    @Test
    fun `When primary text is html should be styled`() {
        val view = HierarchyListItemView(context)

        view.setElement(getHierarchyElement(null, "<h1>Primary text</h1>", ""))

        assertThat(view.binding.primaryText.text.toString(), `is`("Primary text"))
    }

    @Test
    fun `When secondary text is not specified should be gone`() {
        val view = HierarchyListItemView(context)

        // Empty value
        view.setElement(getHierarchyElement(null, "", ""))
        assertThat(view.binding.secondaryText.visibility, `is`(View.GONE))

        // Null value
        view.setElement(getHierarchyElement(null, "", null))
        assertThat(view.binding.secondaryText.visibility, `is`(View.GONE))
    }

    @Test
    fun `When secondary text is specified should be visible`() {
        val view = HierarchyListItemView(context)

        view.setElement(getHierarchyElement(null, "", "Secondary text"))

        assertThat(view.binding.secondaryText.visibility, `is`(View.VISIBLE))
        assertThat(view.binding.secondaryText.text.toString(), `is`("Secondary text"))
    }

    @Test
    fun `When secondary text is html should be styled`() {
        val view = HierarchyListItemView(context)

        view.setElement(getHierarchyElement(null, "", "<h1>Secondary text</h1>"))

        assertThat(view.binding.secondaryText.text.toString(), `is`("Secondary text"))
    }

    private fun getHierarchyElement(icon: Drawable?, primaryText: String, secondaryText: String?) =
        HierarchyElement(primaryText, secondaryText, icon, HierarchyElement.Type.QUESTION, mock())
}
