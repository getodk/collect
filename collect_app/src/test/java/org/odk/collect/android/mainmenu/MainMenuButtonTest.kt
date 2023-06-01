package org.odk.collect.android.mainmenu

import android.app.Application
import android.graphics.Typeface
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class MainMenuButtonTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Material3_Light)
        }

    @Test
    fun `when icon attribute is not used then it is null`() {
        val mainMenuButton = MainMenuButton(context)

        assertThat(mainMenuButton.findViewById<ImageView>(R.id.icon).drawable, equalTo(null))
    }

    @Test
    fun `when icon attribute is used then it is set correctly`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.icon, "@drawable/ic_edit_24").build()
        val mainMenuButton = MainMenuButton(context, attrs)

        assertThat(
            (mainMenuButton.findViewById<ImageView>(R.id.icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
    }

    @Test
    fun `when name attribute is not used then it is empty`() {
        val mainMenuButton = MainMenuButton(context)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).text, equalTo(""))
    }

    @Test
    fun `when name attribute is used then it is set correctly`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.name, "blah").build()
        val mainMenuButton = MainMenuButton(context, attrs)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).text, equalTo("blah"))
    }

    @Test
    fun `setNumberOfForms sets number correctly`() {
        val mainMenuButton = MainMenuButton(context)

        mainMenuButton.setNumberOfForms(10)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).text, equalTo("10"))
    }

    @Test
    fun `setNumberOfForms makes 'name' and 'number' bold if highlightable attr is true`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.highlightable, "true").build()
        val mainMenuButton = MainMenuButton(context, attrs)

        mainMenuButton.setNumberOfForms(10)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).typeface.style, equalTo(Typeface.BOLD))
        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).typeface.style, equalTo(Typeface.BOLD))
    }

    @Test
    fun `setNumberOfForms does not 'make' name and 'number' bold if highlightable attr is false`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.highlightable, "false").build()
        val mainMenuButton = MainMenuButton(context, attrs)

        mainMenuButton.setNumberOfForms(10)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).typeface, equalTo(null))
        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).typeface, equalTo(null))
    }

    @Test
    fun `setNumberOfForms does not make 'name' and 'number' bold if highlightable attr is not set`() {
        val mainMenuButton = MainMenuButton(context)

        mainMenuButton.setNumberOfForms(10)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).typeface, equalTo(null))
        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).typeface, equalTo(null))
    }

    @Test
    fun `setNumberOfForms sets an empty string when number is less than 1`() {
        val mainMenuButton = MainMenuButton(context)

        mainMenuButton.setNumberOfForms(0)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).text, equalTo(""))
    }

    @Test
    fun `setNumberOfForms removes bold typeface form 'name' and 'number' when number is less than 1`() {
        val attrs = Robolectric.buildAttributeSet().addAttribute(R.attr.highlightable, "true").build()
        val mainMenuButton = MainMenuButton(context, attrs)

        mainMenuButton.setNumberOfForms(10)
        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).typeface.style, equalTo(Typeface.BOLD))
        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).typeface.style, equalTo(Typeface.BOLD))

        mainMenuButton.setNumberOfForms(0)
        assertThat(mainMenuButton.findViewById<TextView>(R.id.name).typeface.style, equalTo(Typeface.NORMAL))
        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).typeface.style, equalTo(Typeface.NORMAL))
    }
}
