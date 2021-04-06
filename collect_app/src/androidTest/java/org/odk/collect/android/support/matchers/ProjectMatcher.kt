package org.odk.collect.android.support.matchers

import android.view.View
import android.widget.TextView
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.odk.collect.android.R
import org.odk.collect.android.projects.Project

object ProjectMatcher {
    @JvmStatic
    fun withProject(project: Project): Matcher<View> {
        return object : BoundedMatcher<View, View>(View::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("Project to be $project")
            }

            override fun matchesSafely(view: View): Boolean {
                val displayedProjectIcon = (view.findViewById<TextView>(R.id.project_icon)).text.toString()
                val displayedProjectName = (view.findViewById<TextView>(R.id.project_name)).text.toString()
                assertThat(displayedProjectIcon, `is`(project.icon))
                assertThat(displayedProjectName, `is`(project.name))
                return true
            }
        }
    }
}
