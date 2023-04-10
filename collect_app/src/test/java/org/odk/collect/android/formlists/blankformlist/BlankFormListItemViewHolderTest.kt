package org.odk.collect.android.formlists.blankformlist

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.databinding.BlankFormListItemBinding

@RunWith(AndroidJUnit4::class)
class BlankFormListItemViewHolderTest {

    @Test
    fun `displays form version`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val parent = FrameLayout(context)
        val viewHolder = BlankFormListItemViewHolder(parent)

        viewHolder.blankFormListItem = blankFormListItem(formVersion = "myVersion")

        val binding = BlankFormListItemBinding.bind(viewHolder.itemView)
        assertThat(
            binding.formSubtitle.text,
            equalTo(context.getString(R.string.version_number, "myVersion"))
        )
    }

    @Test
    fun `hides subtitle when form version is blank`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val parent = FrameLayout(context)
        val viewHolder = BlankFormListItemViewHolder(parent)

        viewHolder.blankFormListItem = blankFormListItem(formVersion = "")

        val binding = BlankFormListItemBinding.bind(viewHolder.itemView)
        assertThat(binding.formSubtitle.visibility, equalTo(View.GONE))
    }
}

private fun blankFormListItem(formVersion: String = "formVersion"): BlankFormListItem {
    return BlankFormListItem(
        databaseId = 0,
        formId = "formId",
        formName = "formName",
        formVersion = formVersion,
        geometryPath = "",
        dateOfCreation = 0,
        dateOfLastUsage = 0,
        dateOfLastDetectedAttachmentsUpdate = null,
        contentUri = Uri.parse("http://example.com")
    )
}
