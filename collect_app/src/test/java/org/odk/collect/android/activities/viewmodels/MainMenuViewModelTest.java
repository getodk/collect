package org.odk.collect.android.activities.viewmodels;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class MainMenuViewModelTest {

    @Test
    public void version_whenRelease_returnsSemanticVersionWithPrefix() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.1.7");
        assertThat(viewModel.getVersion(), equalTo("v1.1.7"));
    }

    @Test
    public void version_whenDirtyRelease_returnsSemanticVersionWithPrefix() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.1.7-dirty");
        assertThat(viewModel.getVersion(), equalTo("v1.1.7"));
    }

    @Test
    public void version_whenBetaRelease_returnsSemanticVersionWithPrefix_andBetaVersion() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.23.0-beta.1");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0 Beta 1"));
    }

    @Test
    public void version_whenDirtyBetaRelease_returnsSemanticVersionWithPrefix_andBetaVersion() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.23.0-beta.1-dirty");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0 Beta 1"));
    }

    @Test
    public void version_whenBetaTag_returnsSemanticVersionWithPrefix_andBetaVersion() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.23.0-beta.1-181-ge51d004d4");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0 Beta 1"));
    }

    @Test
    public void version_whenReleaseTag_returnsSemanticVersionWithPrefix() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.23.0-181-ge51d004d4");
        assertThat(viewModel.getVersion(), equalTo("v1.23.0"));
    }

    @Test
    public void versionCommitDescription_whenRelease_returnsNull() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.1.7");
        assertThat(viewModel.getVersionCommitDescription(), equalTo(null));
    }

    @Test
    public void versionCommitDescription_whenDirtyRelease_returnsDirty() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.1.7-dirty");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("dirty"));
    }

    @Test
    public void versionCommitDescription_whenBetaRelease_returnsNull() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.1.7-beta.7");
        assertThat(viewModel.getVersionCommitDescription(), equalTo(null));
    }

    @Test
    public void versionCommitDescription_whenDirtyBetaRelease_returnsNull() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.1.7-beta.7-dirty");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("dirty"));
    }

    @Test
    public void versionCommitDescription_whenBetaTag_returnsCommitCountAndSHA() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.23.0-beta.1-181-ge51d004d4");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("181-ge51d004d4"));
    }

    @Test
    public void versionCommitDescription_whenReleaseTag_returnsCommitCountAndSHA() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.23.0-181-ge51d004d4");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("181-ge51d004d4"));
    }

    @Test
    public void versionCommitDescription_whenDirtyCommit_returnsCommitCountAndSHAAndDirtyTag() {
        MainMenuViewModel viewModel = new MainMenuViewModel(() -> "v1.14.0-181-ge51d004d4-dirty");
        assertThat(viewModel.getVersionCommitDescription(), equalTo("181-ge51d004d4-dirty"));
    }
}