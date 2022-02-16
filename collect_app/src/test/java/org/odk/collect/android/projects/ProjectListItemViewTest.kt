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
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class ProjectListItemViewTest {

    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }

    @Test
    fun `shows project name`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_USERNAME) } doReturn ""
            on { getString(ProjectKeys.KEY_SERVER_URL) } doReturn ""
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_name).text, equalTo("SOM"))
    }

    @Test
    fun `shows project icon with color as background`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_USERNAME) } doReturn ""
            on { getString(ProjectKeys.KEY_SERVER_URL) } doReturn ""
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
            on { getString(ProjectKeys.KEY_USERNAME) } doReturn "Adam"
            on { getString(ProjectKeys.KEY_SERVER_URL) } doReturn "https://my-project.com"
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("Adam / my-project.com"))
    }

    @Test
    fun `shows project only url if username is not set`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_USERNAME) } doReturn ""
            on { getString(ProjectKeys.KEY_SERVER_URL) } doReturn "https://my-project.com"
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("my-project.com"))
    }

    @Test
    fun `shows username only if url is not set`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_USERNAME) } doReturn "foo@bar.baz"
            on { getString(ProjectKeys.KEY_SERVER_URL) } doReturn ""
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("foo@bar.baz / "))
    }

    @Test
    fun `passes through URL value that can't be parsed as URL`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_USERNAME) } doReturn "foo"
            on { getString(ProjectKeys.KEY_SERVER_URL) } doReturn "something something"
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("foo / something something"))
    }

    @Test
    fun `shows Google account and "Google Drive" if protocol is Google Drive`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_PROTOCOL) } doReturn ProjectKeys.PROTOCOL_GOOGLE_SHEETS
            on { getString(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT) } doReturn "foo@bar.baz"
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("foo@bar.baz / Google Drive"))
    }

    @Test
    fun `shows "Google Drive" if protocol is Google Drive and username is not set`() {
        val generalSettings = mock<Settings> {
            on { getString(ProjectKeys.KEY_PROTOCOL) } doReturn ProjectKeys.PROTOCOL_GOOGLE_SHEETS
        }

        val view = ProjectListItemView(context)
        view.setupView(Project.New("SOM", "S", "#ffffff"), generalSettings)
        assertThat(view.findViewById<TextView>(R.id.project_subtext).text, equalTo("Google Drive"))
    }
}
