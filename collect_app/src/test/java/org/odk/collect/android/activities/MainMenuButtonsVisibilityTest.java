package org.odk.collect.android.activities;

import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.shared.Settings;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_DELETE_SAVED;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_EDIT_SAVED;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_GET_BLANK;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_SEND_FINALIZED;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_VIEW_SENT;

@RunWith(RobolectricTestRunner.class)
public class MainMenuButtonsVisibilityTest {

    private MainMenuActivity mainMenuActivity;
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();

    @Before
    public void setup() {
        adminSettings.clear();
        adminSettings.setDefaultForAllSettingsWithoutValues();
    }

    @Test
    public void when_editSavedFormButtonIsEnabledInSettings_shouldBeVisible() {
        createActivity();

        Button editSavedFormButton = mainMenuActivity.findViewById(R.id.review_data);
        assertThat(editSavedFormButton.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void when_editSavedFormButtonIsDisabledInSettings_shouldBeGone() {
        adminSettings.save(KEY_EDIT_SAVED, false);
        createActivity();

        Button editSavedFormButton = mainMenuActivity.findViewById(R.id.review_data);
        assertThat(editSavedFormButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void when_sendFinalizedFormButtonIsEnabledInSettings_shouldBeVisible() {
        createActivity();

        Button sendFinalizedFormButton = mainMenuActivity.findViewById(R.id.send_data);
        assertThat(sendFinalizedFormButton.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void when_sendFinalizedFormButtonIsDisabledInSettings_shouldBeGone() {
        adminSettings.save(KEY_SEND_FINALIZED, false);
        createActivity();

        Button sendFinalizedFormButton = mainMenuActivity.findViewById(R.id.send_data);
        assertThat(sendFinalizedFormButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void when_viewSentFormButtonIsEnabledInSettings_shouldBeVisible() {
        createActivity();

        Button viewSentFormButton = mainMenuActivity.findViewById(R.id.view_sent_forms);
        assertThat(viewSentFormButton.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void when_viewSentFormButtonIsDisabledInSettings_shouldBeGone() {
        adminSettings.save(KEY_VIEW_SENT, false);
        createActivity();

        Button viewSentFormButton = mainMenuActivity.findViewById(R.id.view_sent_forms);
        assertThat(viewSentFormButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void when_getBlankFormButtonIsEnabledInSettings_shouldBeVisible() {
        createActivity();

        Button getBlankFormButton = mainMenuActivity.findViewById(R.id.get_forms);
        assertThat(getBlankFormButton.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void when_getBlankFormButtonIsDisabledInSettings_shouldBeGone() {
        adminSettings.save(KEY_GET_BLANK, false);
        createActivity();

        Button getBlankFormButton = mainMenuActivity.findViewById(R.id.get_forms);
        assertThat(getBlankFormButton.getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void when_deleteSavedFormButtonIsEnabledInSettings_shouldBeVisible() {
        createActivity();

        Button deleteSavedFormButton = mainMenuActivity.findViewById(R.id.manage_forms);
        assertThat(deleteSavedFormButton.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void when_deleteSavedFormButtonIsDisabledInSettings_shouldBeGone() {
        adminSettings.save(KEY_DELETE_SAVED, false);
        createActivity();

        Button deleteSavedFormButton = mainMenuActivity.findViewById(R.id.manage_forms);
        assertThat(deleteSavedFormButton.getVisibility(), equalTo(View.GONE));
    }

    private void createActivity() {
        mainMenuActivity = Robolectric
                .buildActivity(MainMenuActivity.class)
                .create()
                .resume()
                .get();
    }
}
