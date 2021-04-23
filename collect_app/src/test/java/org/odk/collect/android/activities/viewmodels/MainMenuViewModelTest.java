package org.odk.collect.android.activities.viewmodels;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formmanagement.InstancesCountRepository;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.projects.Project;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainMenuViewModelTest {
    private final CurrentProjectProvider currentProjectProvider = mock(CurrentProjectProvider.class);

    @Test
    public void version_whenBetaRelease_returnsSemanticVersionWithPrefix_andBetaVersion() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.23.0-beta.1");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0 Beta 1"));
    }

    @Test
    public void version_whenDirtyBetaRelease_returnsSemanticVersionWithPrefix_andBetaVersion() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.23.0-beta.1-dirty");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0 Beta 1"));
    }

    @Test
    public void version_whenBetaTag_returnsSemanticVersionWithPrefix_andBetaVersion() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0 Beta 1"));
    }

    @Test
    public void versionCommitDescription_whenRelease_returnsNull() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.1.7");
        assertThat(viewModel.getVersionCommitDescription(), equalTo(null));
    }

    @Test
    public void versionCommitDescription_whenDirtyRelease_returnsDirty() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.1.7-dirty");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("dirty"));
    }

    @Test
    public void versionCommitDescription_whenBetaRelease_returnsNull() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.1.7-beta.7");
        assertThat(viewModel.getVersionCommitDescription(), equalTo(null));
    }

    @Test
    public void versionCommitDescription_whenDirtyBetaRelease_returnsNull() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.1.7-beta.7-dirty");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("dirty"));
    }

    @Test
    public void versionCommitDescription_whenBetaTag_returnsCommitCountAndSHA() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("181-ge51d004d4"));
    }

    @Test
    public void versionCommitDescription_whenReleaseTag_returnsCommitCountAndSHA() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.23.0-181-ge51d004d4");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("181-ge51d004d4"));
    }

    @Test
    public void versionCommitDescription_whenDirtyCommit_returnsCommitCountAndSHAAndDirtyTag() {
        MainMenuViewModel viewModel = createViewModelWithVersion("v1.14.0-181-ge51d004d4-dirty");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("181-ge51d004d4-dirty"));
    }

    @Test
    public void getCurrentProject_shouldReturnCurrentProject() {
        Project project = new Project("Project 1", "P", "#ffffff", "1");
        when(currentProjectProvider.getCurrentProject()).thenReturn(project);

        MainMenuViewModel viewModel = createViewModelWithVersion("");
        assertThat(viewModel.getCurrentProject(), equalTo(project));
    }

    @NotNull
    private MainMenuViewModel createViewModelWithVersion(String version) {
        return new MainMenuViewModel(mock(Application.class), new VersionInformation(() -> version), mock(SettingsProvider.class), mock(InstancesCountRepository.class), currentProjectProvider);
    }
}
