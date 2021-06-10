package org.odk.collect.android.projects

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectDetailsCreatorTest {

    val projectDetailsCreator = ProjectDetailsCreator(ApplicationProvider.getApplicationContext())

    @Test
    fun `Project name should be generated from url`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").name, `is`("my-project.com"))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").name, `is`("your-project.com"))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").name, `is`("www.my-project.com"))
    }

    @Test
    fun `Project icon should be generated from url`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").icon, `is`("M"))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").icon, `is`("Y"))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").icon, `is`("W"))
    }

    @Test
    fun `Generated project color should be the same for identical project names`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").color, `is`(projectDetailsCreator.getProject("https://my-project.com").color))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").color, `is`(projectDetailsCreator.getProject("https://your-project.com/one").color))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").color, `is`(projectDetailsCreator.getProject("http://www.my-project.com").color))
    }

    @Test
    fun `Generated project color should be different for different project names`() {
        assertThat(projectDetailsCreator.getProject("https://my-project.com").color, not(projectDetailsCreator.getProject("http://www.my-project.com").color))
        assertThat(projectDetailsCreator.getProject("https://your-project.com/one").color, not(projectDetailsCreator.getProject("http://www.my-project.com").color))
        assertThat(projectDetailsCreator.getProject("http://www.my-project.com").color, not(projectDetailsCreator.getProject("https://your-project.com/one").color))
    }
}
