package org.odk.collect.android.mainmenu

import android.app.Application
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R

@RunWith(AndroidJUnit4::class)
class MainMenuButtonTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Material3_Light)
        }

    private val mainMenuButton = MainMenuButton(context)

    @Test
    fun `setNumberOfForms sets the number correctly`() {
        mainMenuButton.setNumberOfForms(10)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).text, equalTo("10"))
    }

    @Test
    fun `setNumberOfForms sets an empty string when number is less than 1`() {
        mainMenuButton.setNumberOfForms(0)

        assertThat(mainMenuButton.findViewById<TextView>(R.id.number).text, equalTo(""))
    }
}
