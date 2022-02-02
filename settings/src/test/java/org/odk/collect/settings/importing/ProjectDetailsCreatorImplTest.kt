package org.odk.collect.settings.importing

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.ProjectKeys

class ProjectDetailsCreatorImplTest {

    private val projectDetailsCreator = ProjectDetailsCreatorImpl(
        listOf(
            "#EA4633",
            "#9E9E9E"
        ),
        mapOf<String, Any>(
            ProjectKeys.KEY_SERVER_URL to "http://default.example.com"
        )
    )

    @Test
    fun `If project name is included in project details should be used`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X").name, `is`("Project X"))
    }

    @Test
    fun `When no project name is specified and the connection identifier is a valid URL, the project name is the URL domain`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "https://my-project.com").name, `is`("my-project.com"))
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "https://your-project.com/one").name, `is`("your-project.com"))
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "http://www.my-project.com").name, `is`("www.my-project.com"))
    }

    @Test
    fun `When no project name is specified and the connection identifier is not a valid URL, the project name is the connection identifier`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "foo@bar.baz").name, `is`("foo@bar.baz"))
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "something").name, `is`("something"))
    }

    @Test
    fun `When no project name is specified and the connection identifier is the demo project explicitly or by default, the project name is 'Demo project'`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "http://default.example.com").name, `is`(Project.DEMO_PROJECT_NAME))
        assertThat(projectDetailsCreator.createProjectFromDetails(connectionIdentifier = "").name, `is`(Project.DEMO_PROJECT_NAME))
    }

    @Test
    fun `If project name is empty should be generated based on connection identifier`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = " ", connectionIdentifier = "https://my-server.com").name, `is`("my-server.com"))
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
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X").color, `is`("#9E9E9E"))
    }

    @Test
    fun `If project color is not included in project details and project name is demo the color should be demo color`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = Project.DEMO_PROJECT_NAME).color, `is`(Project.DEMO_PROJECT_COLOR))
    }

    @Test
    fun `If project color is included in project details but invalid should be generated based on project name`() {
        assertThat(projectDetailsCreator.createProjectFromDetails(name = "Project X", icon = "#cc").color, `is`("#9E9E9E"))
    }
}
