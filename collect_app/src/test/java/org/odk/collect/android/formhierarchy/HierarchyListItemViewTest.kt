package org.odk.collect.android.formhierarchy

import android.app.Application
import android.view.View
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.core.model.FormIndex
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.utilities.HtmlUtils

@RunWith(AndroidJUnit4::class)
class HierarchyListItemViewTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(com.google.android.material.R.style.Theme_Material3_Light)
        }

    @Test
    fun `Question item should contain only primary text and secondary text`() {
        val view = HierarchyListItemView(context, HierarchyItemType.QUESTION.id)
        composeRule.setContent {
            AndroidView(factory = {
                view.also {
                    it.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.QUESTION, "foo", "bar", mock<FormEntryPrompt>()), mock<ViewModelProvider>(), {})
                }
            })
        }

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<ComposeView>(R.id.answer_view).visibility, equalTo(View.VISIBLE))

        composeRule.onNodeWithText("bar").assertExists()

        assertThat(view.findViewById<ImageView>(R.id.icon), equalTo(null))
    }

    @Test
    fun `When secondary text is html should be styled`() {
        val view = HierarchyListItemView(context, HierarchyItemType.QUESTION.id)
        composeRule.setContent {
            AndroidView(factory = {
                view.also {
                    it.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.QUESTION, "foo", "<h1>bar</h1>", mock<FormEntryPrompt>()), mock<ViewModelProvider>(), {})
                }
            })
        }

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        val styledText = AnnotatedString.fromHtml(HtmlUtils.markdownToHtml("<h1>bar</h1>"))
        composeRule.onNodeWithText(styledText.text).assertExists()
    }

    @Test
    fun `Group item should contain group label, primary text and icon`() {
        val view = HierarchyListItemView(context, HierarchyItemType.VISIBLE_GROUP.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.VISIBLE_GROUP, "foo"), mock<ViewModelProvider>(), {})

        assertThat(view.findViewById<ImageView>(R.id.icon).visibility, equalTo(View.VISIBLE))

        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).text.toString(), equalTo(context.getString(org.odk.collect.strings.R.string.group_label)))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))
    }

    @Test
    fun `Repeatable group item should contain group label, primary text and icon`() {
        val view = HierarchyListItemView(context, HierarchyItemType.REPEATABLE_GROUP.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.REPEATABLE_GROUP, "foo"), mock<ViewModelProvider>(), {})

        assertThat(view.findViewById<ImageView>(R.id.icon).visibility, equalTo(View.VISIBLE))

        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.group_label).text.toString(), equalTo(context.getString(org.odk.collect.strings.R.string.repeatable_group_label)))

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))
    }

    @Test
    fun `Repeatable group instance item should contain only primary text`() {
        val view = HierarchyListItemView(context, HierarchyItemType.REPEAT_INSTANCE.id)

        view.setElement(HierarchyItem(mock<FormIndex>(), HierarchyItemType.REPEAT_INSTANCE, "foo"), mock<ViewModelProvider>(), {})

        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).visibility, equalTo(View.VISIBLE))
        assertThat(view.findViewById<MaterialTextView>(R.id.primary_text).text.toString(), equalTo("foo"))

        assertThat(view.findViewById<ImageView>(R.id.icon), equalTo(null))
        assertThat(view.findViewById<ComposeView>(R.id.answer_view), equalTo(null))
    }
}
