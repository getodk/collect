package org.odk.collect.android.mainmenu

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.version.VersionInformation

@RunWith(AndroidJUnit4::class)
class MainMenuViewModelTest {
    @Test
    fun `version when beta release returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `version when dirty beta release returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-dirty")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `version when beta tag returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `versionCommitDescription when release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7")
        assertThat(viewModel.versionCommitDescription, equalTo(null))
    }

    @Test
    fun `versionCommitDescription when dirty release returns dirty`() {
        val viewModel = createViewModelWithVersion("v1.1.7-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("dirty"))
    }

    @Test
    fun `versionCommitDescription when beta release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7-beta.7")
        assertThat(viewModel.versionCommitDescription, equalTo(null))
    }

    @Test
    fun `versionCommitDescription when dirty beta release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7-beta.7-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("dirty"))
    }

    @Test
    fun `versionCommitDescription when beta tag returns commit count and SHA`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4"))
    }

    @Test
    fun `versionCommitDescription when release tag returns commit count and SHA`() {
        val viewModel = createViewModelWithVersion("v1.23.0-181-ge51d004d4")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4"))
    }

    @Test
    fun `versionCommitDescription when dirty commit returns commit count and SHA and dirty tag`() {
        val viewModel = createViewModelWithVersion("v1.14.0-181-ge51d004d4-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4-dirty"))
    }

    private fun createViewModelWithVersion(version: String): MainMenuViewModel {
        return MainMenuViewModel(mock(), VersionInformation { version }, mock(), mock(), mock())
    }
}
