package org.odk.collect.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class ProjectDetailsGeneratorTest {

    @Test
    fun `Project name should be extracted from url`() {
        assertThat(ProjectDetailsGenerator.generateProjectDetails("https://demo.getodk.org").projectName, `is`("demo.getodk.org"))
        assertThat(ProjectDetailsGenerator.generateProjectDetails("http://ona.io/somebody").projectName, `is`("ona.io"))
        assertThat(ProjectDetailsGenerator.generateProjectDetails("https://www.ona.io").projectName, `is`("www.ona.io"))
        assertThat(ProjectDetailsGenerator.generateProjectDetails("http://test.getodk.org/v1/key/jfkdlsajfaioehafnelsajfkdsljfs;jfsa/projects/17").projectName, `is`("test.getodk.org"))
    }

    @Test
    fun `Project icon should be extracted from url`() {
        assertThat(ProjectDetailsGenerator.generateProjectDetails("https://demo.getodk.org").projectIcon, `is`("D"))
        assertThat(ProjectDetailsGenerator.generateProjectDetails("http://ona.io/somebody").projectIcon, `is`("O"))
        assertThat(ProjectDetailsGenerator.generateProjectDetails("https://www.ona.io").projectIcon, `is`("W"))
        assertThat(ProjectDetailsGenerator.generateProjectDetails("http://test.getodk.org/v1/key/jfkdlsajfaioehafnelsajfkdsljfs;jfsa/projects/17").projectIcon, `is`("T"))
    }
}
