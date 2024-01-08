package org.odk.collect.android.formhierarchy

import android.app.Application
import android.view.View
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormIndex
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.R

@RunWith(AndroidJUnit4::class)
class HierarchyListItemViewTest {

    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(com.google.android.material.R.style.Theme_Material3_Light)
        }

    @Test
    fun `Question item should contain only primary text and secondary text`() {
        val view = HierarchyListItemView(context, HierarchyItemType.QUESTION.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.QUESTION, "foo", "bar"))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text).text.toString(), equalTo("bar"))

        assertThat(view.findViewById<ImageView>(R.id.icon), equalTo(null))
    }

    @Test
    fun `When secondary text is not set the textview should be gone`() {
        val view = HierarchyListItemView(context, HierarchyItemType.QUESTION.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.QUESTION, "foo"))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text).visibility, equalTo(View.GONE))
    }

    @Test
    fun `When secondary text is blank the textview should be gone`() {
        val view = HierarchyListItemView(context, HierarchyItemType.QUESTION.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.QUESTION, "foo", " "))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text).visibility, equalTo(View.GONE))
    }

    @Test
    fun `When secondary text is html should be styled`() {
        val view = HierarchyListItemView(context, HierarchyItemType.QUESTION.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.QUESTION, "foo", "<h1>bar</h1>"))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text).text.toString(), equalTo("bar"))
    }

    @Test
    fun `Group item should contain group label, primary text and icon`() {
        val view = HierarchyListItemView(context, HierarchyItemType.VISIBLE_GROUP.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.VISIBLE_GROUP, "foo"))

        assertThat(view.findViewById<ImageView>(R.id.icon).visibility, equalTo(View.VISIBLE))

        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).text.toString(), equalTo(context.getString(org.odk.collect.strings.R.string.group_label)))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))
    }

    @Test
    fun `Repeatable group item should contain group label, primary text and icon`() {
        val view = HierarchyListItemView(context, HierarchyItemType.REPEATABLE_GROUP.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.REPEATABLE_GROUP, "foo"))

        assertThat(view.findViewById<ImageView>(R.id.icon).visibility, equalTo(View.VISIBLE))

        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).text.toString(), equalTo(context.getString(org.odk.collect.strings.R.string.repeatable_group_label)))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))
    }

    @Test
    fun `Repeatable group instance item should contain only primary text`() {
        val view = HierarchyListItemView(context, HierarchyItemType.REPEAT_INSTANCE.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.REPEAT_INSTANCE, "foo"))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<ImageView>(R.id.icon), equalTo(null))
        assertThat(view.findViewById<MaterialTextView>(R.id.secondary_text), equalTo(null))
    }
}
