package org.odk.collect.android.projects

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class ProjectDetailsCreatorTest {

    val projectDetailsCreator = ProjectDetailsCreator(ApplicationProvider.getApplicationContext())

    @Test
    fun `If project name is included in project details should be used`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X").name, `is`("Project X"))
    }

    @Test
    fun `If project name is not included in project details should be generated based on url`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://my-server.com").name, `is`("my-server.com"))
    }

    @Test
    fun `If project name is empty should be generated based on url`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://my-server.com", name = " ").name, `is`("my-server.com"))
    }

    @Test
    fun `If project icon is included in project details and it's a normal char should be used`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(icon = "X").icon, `is`("X"))
    }

    @Test
    fun `If project icon is included in project details and it's an emoticon should be used`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(icon = "\uD83D\uDC22").icon, `is`("\uD83D\uDC22"))
    }

    @Test
    fun `If project icon is not included in project details should be generated based on project name`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X").icon, `is`("P"))
    }

    @Test
    fun `If project icon is empty should be generated based on project name`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "My Project X", icon = " ").icon, `is`("M"))
    }

    @Test
    fun `If project icon is included in project details but longer than one sign, only the first sign should be used`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(icon = "XX").icon, `is`("X"))
        assertThat(projectDetailsCreator.createProjectFromDetails(icon = "\uD83D\uDC22XX").icon, `is`("\uD83D\uDC22"))
    }

    @Test
    fun `If project color is included in project details should be used`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(color = "#cccccc").color, `is`("#cccccc"))
    }

    @Test
    fun `If project color is not included in project details should be generated based on project name`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X").color, `is`("#9f50b0"))
    }

    @Test
    fun `If project color is not included in project details and project name is demo the color should be demo color`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = Project.DEMO_PROJECT_NAME).color, `is`(Project.DEMO_PROJECT_COLOR))
    }

    @Test
    fun `If project color is included in project details but invalid should be generated based on project name`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X", icon = "#cc").color, `is`("#9f50b0"))
    }

    @Test
    fun `Test generating project name from various urls`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://my-project.com").name, `is`("my-project.com"))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://your-project.com/one").name, `is`("your-project.com"))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "http://www.my-project.com").name, `is`("www.my-project.com"))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://demo.getodk.org").name, `is`(Project.DEMO_PROJECT_NAME))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "").name, `is`(Project.DEMO_PROJECT_NAME))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "something").name, `is`("Project")) // default project name for invalid urls
    }

    @Test
    fun `Generated project color should be the same for identical project names`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://my-project.com").color, `is`(projectDetailsCreator.createProjectFromDetails(url = "https://my-project.com").color))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://your-project.com/one").color, `is`(projectDetailsCreator.createProjectFromDetails(url = "https://your-project.com/one").color))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "http://www.my-project.com").color, `is`(projectDetailsCreator.createProjectFromDetails(url = "http://www.my-project.com").color))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "qwerty").color, `is`(projectDetailsCreator.createProjectFromDetails(url = "something").color)) // default project color for invalid urls
    }

    @Test
    fun `Generated project color should be different for different project names`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://my-project.com").color, not(projectDetailsCreator.createProjectFromDetails(url = "http://www.my-project.com").color))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "https://your-project.com/one").color, not(projectDetailsCreator.createProjectFromDetails(url = "http://www.my-project.com").color))
        assertThat(projectDetailsCreator.createProjectFromDetails(url = "http://www.my-project.com").color, not(projectDetailsCreator.createProjectFromDetails(url = "https://your-project.com/one").color))
    }
}
