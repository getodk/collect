package org.odk.collect.android.activities.viewmodels;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.version.VersionInformation;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class MainMenuViewModelTest {

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

    @NotNull
    private MainMenuViewModel createViewModelWithVersion(String version) {
        return new MainMenuViewModel(mock(Application.class), new VersionInformation(() -> version), mock(PreferencesProvider.class));
    }
}