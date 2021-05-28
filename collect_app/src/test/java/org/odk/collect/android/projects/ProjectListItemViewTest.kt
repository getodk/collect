package org.odk.collect.android.projects

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.Project
import org.odk.collect.shared.Settings

@RunWith(AndroidJUnit4::class)
class ProjectListItemViewTest {

    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }

    @Test
    fun `shows project name`() {
        val generalSettings = mock<Settings> {
            on { getString(GeneralKeys.KEY_USERNAME) } doReturn ""
            on { getString(GeneralKeys.KEY_SERVER_URL) } doReturn ""
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_name).text, equalTo("SOM"))
    }

    @Test
    fun `shows project icon with color as background`() {
        val generalSettings = mock<Settings> {
            on { getString(GeneralKeys.KEY_USERNAME) } doReturn ""
            on { getString(GeneralKeys.KEY_SERVER_URL) } doReturn ""
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_icon_text).text, equalTo("S"))

        val background = view.findViewById<TextView>(R.id.project_icon_text).background as GradientDrawable
        assertThat(background.color!!.defaultColor, equalTo(Color.parseColor("#ffffff")))
    }

    @Test
    fun `shows project username and url`() {
        val generalSettings = mock<Settings> {
            on { getString(GeneralKeys.KEY_USERNAME) } doReturn "Adam"
            on { getString(GeneralKeys.KEY_SERVER_URL) } doReturn "https://my-project.com"
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("Adam / my-project.com"))
    }

    @Test
    fun `shows project only url if username is not set`() {
        val generalSettings = mock<Settings> {
            on { getString(GeneralKeys.KEY_USERNAME) } doReturn ""
            on { getString(GeneralKeys.KEY_SERVER_URL) } doReturn "https://my-project.com"
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("my-project.com"))
    }
}
