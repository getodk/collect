package org.odk.collect.androidshared.ui

import android.app.Application
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.androidshared.R
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class EmptyListViewTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(com.google.android.material.R.style.Theme_Material3_Light)
        }

    @Test
    fun `when icon attribute is not used then it is null`() {
        val emptyListView = EmptyListView(context)

        assertThat(emptyListView.findViewById<ImageView>(R.id.icon).drawable, equalTo(null))
    }

    @Test
    fun `when icon attribute is used then it is set correctly`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.icon, "@drawable/ic_baseline_warning_24").build()
        val emptyListView = EmptyListView(context, attrs)

        assertThat(
            (emptyListView.findViewById<ImageView>(R.id.icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_baseline_warning_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
    }

    @Test
    fun `when title attribute is not used then it is empty`() {
        val emptyListView = EmptyListView(context)

        assertThat(emptyListView.findViewById<MaterialTextView>(R.id.title).text, equalTo(""))
    }

    @Test
    fun `when title attribute is used then it is set correctly`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.title, "blah").build()
        val emptyListView = EmptyListView(context, attrs)

        assertThat(emptyListView.findViewById<MaterialTextView>(R.id.title).text, equalTo("blah"))
    }

    @Test
    fun `when subtitle attribute is not used then it is empty`() {
        val emptyListView = EmptyListView(context)

        assertThat(emptyListView.findViewById<MaterialTextView>(R.id.subtitle).text, equalTo(""))
    }

    @Test
    fun `when subtitle attribute is used then it is set correctly`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.subtitle, "blah").build()
        val emptyListView = EmptyListView(context, attrs)

        assertThat(emptyListView.findViewById<MaterialTextView>(R.id.subtitle).text, equalTo("blah"))
    }

    @Test
    fun `icon can be set programmatically`() {
        val emptyListView = EmptyListView(context)
        emptyListView.setIcon(R.drawable.ic_baseline_warning_24)

        assertThat(
            (emptyListView.findViewById<ImageView>(R.id.icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_baseline_warning_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
    }

    @Test
    fun `title can be set programmatically`() {
        val emptyListView = EmptyListView(context)
        emptyListView.setTitle("blah")

        assertThat(emptyListView.findViewById<MaterialTextView>(R.id.title).text, equalTo("blah"))
    }

    @Test
    fun `subtitle can be set programmatically`() {
        val emptyListView = EmptyListView(context)
        emptyListView.setSubtitle("blah")

        assertThat(emptyListView.findViewById<MaterialTextView>(R.id.subtitle).text, equalTo("blah"))
    }
}
