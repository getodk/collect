package org.odk.collect.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class ProjectDetailsGeneratorTest {

    @Test
    fun `Project name should be extracted from url if url is valid`() {
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("https://my-server.com").first, `is`("my-server"))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("http://my-server.com").first, `is`("my-server"))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("https://my-server.something.com").first, `is`("my-server"))
    }

    @Test
    fun `Project name should not be extracted from url if url is invalid`() {
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("").first, `is`(""))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("httpp://my-server.com").first, `is`(""))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("my-server.com").first, `is`(""))
    }

    @Test
    fun `Project icon should be extracted from url if url is invalid`() {
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("https://my-server.com").second, `is`("M"))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("http://my-server.com").second, `is`("M"))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("https://my-server.something.com").second, `is`("M"))
    }

    @Test
    fun `Project icon should not be extracted from url if url is invalid`() {
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("").second, `is`(""))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("httpp://my-server.com").second, `is`(""))
        assertThat(ProjectDetailsGenerator.getProjectNameAndIconFromUrl("my-server.com").second, `is`(""))
    }
}
