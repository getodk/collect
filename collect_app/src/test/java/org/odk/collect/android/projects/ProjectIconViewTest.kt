package org.odk.collect.android.projects

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class ProjectIconViewTest {

    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }

    @Test
    fun `shows project icon with color as background`() {
        val view = ProjectIconView(context)
        view.project = Project.New("SOM", "S", "#ffffff")
        assertThat(view.findViewById<TextView>(R.id.project_icon_text).text, equalTo("S"))

        val background = view.findViewById<TextView>(R.id.project_icon_text).background as GradientDrawable
        assertThat(background.color!!.defaultColor, Matchers.equalTo(Color.parseColor("#ffffff")))
    }
}
