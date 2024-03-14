package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BlankFormListItemViewTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `displays form version`() {
        val view = BlankFormListItemView(context)
        view.setItem(blankFormListItem(formId = "myId", formVersion = "myVersion"))

        assertThat(
            view.binding.formVersion.text,
            equalTo(context.getString(org.odk.collect.strings.R.string.version_number, "myVersion"))
        )
    }

    @Test
    fun `hides version when form version is blank`() {
        val view = BlankFormListItemView(context)
        view.setItem(blankFormListItem(formId = "myId", formVersion = ""))

        assertThat(view.binding.formVersion.visibility, equalTo(View.GONE))
    }

    @Test
    fun `displays form id`() {
        val view = BlankFormListItemView(context)
        view.setItem(blankFormListItem(formId = "myId"))

        assertThat(
            view.binding.formId.text,
            equalTo(context.getString(org.odk.collect.strings.R.string.id_number, "myId"))
        )
    }
}

private fun blankFormListItem(
    formId: String = "formId",
    formVersion: String = "formVersion"
): BlankFormListItem {
    return BlankFormListItem(
        databaseId = 0,
        formId = formId,
        formName = "formName",
        formVersion = formVersion,
        geometryPath = "",
        dateOfCreation = 0,
        dateOfLastUsage = 0,
        dateOfLastDetectedAttachmentsUpdate = null,
        contentUri = Uri.parse("http://example.com")
    )
}
