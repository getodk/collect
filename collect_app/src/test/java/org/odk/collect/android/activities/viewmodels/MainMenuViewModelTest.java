package org.odk.collect.android.activities.viewmodels;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.preferences.AdminKeys.KEY_DELETE_SAVED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_GET_BLANK;
import static org.odk.collect.android.preferences.AdminKeys.KEY_SEND_FINALIZED;
import static org.odk.collect.android.preferences.AdminKeys.KEY_VIEW_SENT;

@RunWith(RobolectricTestRunner.class)
public class MainMenuViewModelTest {

    private final AdminSharedPreferences adminSharedPreferences = mock(AdminSharedPreferences.class);
    private MainMenuViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new MainMenuViewModel(adminSharedPreferences);
    }

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

    @Test
    public void when_editSavedFormButtonIsEnabledInSettings_shouldEditSavedFormButtonBeVisibleReturnTrue() {
        when(adminSharedPreferences.get(KEY_EDIT_SAVED)).thenReturn(true);
        assertThat(viewModel.shouldEditSavedFormButtonBeVisible(), is(true));
    }

    @Test
    public void when_editSavedFormButtonIsDisabledInSettings_shouldEditSavedFormButtonBeVisibleFalse() {
        when(adminSharedPreferences.get(KEY_EDIT_SAVED)).thenReturn(false);
        assertThat(viewModel.shouldEditSavedFormButtonBeVisible(), is(false));
    }

    @Test
    public void when_sendFinalizedFormButtonIsEnabledInSettings_shouldSendFinalizedFormButtonBeVisibleReturnTrue() {
        when(adminSharedPreferences.get(KEY_SEND_FINALIZED)).thenReturn(true);
        assertThat(viewModel.shouldSendFinalizedFormButtonBeVisible(), is(true));
    }

    @Test
    public void when_sendFinalizedFormButtonIsDisabledInSettings_shouldSendFinalizedFormButtonBeVisibleReturnFalse() {
        when(adminSharedPreferences.get(KEY_SEND_FINALIZED)).thenReturn(false);
        assertThat(viewModel.shouldSendFinalizedFormButtonBeVisible(), is(false));
    }

    @Test
    public void when_viewSentFormButtonIsEnabledInSettings_shouldViewSentFormButtonBeVisibleReturnTrue() {
        when(adminSharedPreferences.get(KEY_VIEW_SENT)).thenReturn(true);
        assertThat(viewModel.shouldViewSentFormButtonBeVisible(), is(true));
    }

    @Test
    public void when_viewSentFormButtonIsDisabledInSettings_shouldViewSentFormButtonBeVisibleReturnFalse() {
        when(adminSharedPreferences.get(KEY_VIEW_SENT)).thenReturn(false);
        assertThat(viewModel.shouldViewSentFormButtonBeVisible(), is(false));
    }

    @Test
    public void when_getBlankFormButtonIsEnabledInSettings_shouldGetBlankFormButtonBeVisibleReturnTrue() {
        when(adminSharedPreferences.get(KEY_GET_BLANK)).thenReturn(true);
        assertThat(viewModel.shouldGetBlankFormButtonBeVisible(), is(true));
    }

    @Test
    public void when_getBlankFormButtonIsDisabledInSettings_shouldGetBlankFormButtonBeVisibleReturnFalse() {
        when(adminSharedPreferences.get(KEY_GET_BLANK)).thenReturn(false);
        assertThat(viewModel.shouldGetBlankFormButtonBeVisible(), is(false));
    }

    @Test
    public void when_deleteSavedFormButtonIsEnabledInSettings_shouldDeleteSavedFormButtonBeVisibleReturnTrue() {
        when(adminSharedPreferences.get(KEY_DELETE_SAVED)).thenReturn(true);
        assertThat(viewModel.shouldDeleteSavedFormButtonBeVisible(), is(true));
    }

    @Test
    public void when_deleteSavedFormButtonIsDisabledInSettings_shouldDeleteSavedFormButtonBeVisibleReturnFalse() {
        when(adminSharedPreferences.get(KEY_DELETE_SAVED)).thenReturn(false);
        assertThat(viewModel.shouldDeleteSavedFormButtonBeVisible(), is(false));
    }
}