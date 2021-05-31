package org.odk.collect.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class ProjectGeneratorTest {

    @Test
    fun `Project name should be extracted from url`() {
        assertThat(ProjectGenerator.generateProject("https://demo.getodk.org").name, `is`("demo.getodk.org"))
        assertThat(ProjectGenerator.generateProject("http://ona.io/somebody").name, `is`("ona.io"))
        assertThat(ProjectGenerator.generateProject("https://www.ona.io").name, `is`("www.ona.io"))
        assertThat(ProjectGenerator.generateProject("http://test.getodk.org/v1/key/jfkdlsajfaioehafnelsajfkdsljfs;jfsa/projects/17").name, `is`("test.getodk.org"))
    }

    @Test
    fun `Project icon should be extracted from url`() {
        assertThat(ProjectGenerator.generateProject("https://demo.getodk.org").icon, `is`("D"))
        assertThat(ProjectGenerator.generateProject("http://ona.io/somebody").icon, `is`("O"))
        assertThat(ProjectGenerator.generateProject("https://www.ona.io").icon, `is`("W"))
        assertThat(ProjectGenerator.generateProject("http://test.getodk.org/v1/key/jfkdlsajfaioehafnelsajfkdsljfs;jfsa/projects/17").icon, `is`("T"))
    }
}
