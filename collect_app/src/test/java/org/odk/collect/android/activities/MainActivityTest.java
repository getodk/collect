package org.odk.collect.android.activities;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Unit test for checking {@link Button}'s behaviour  in {@link MainMenuActivity}
 */
@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    private MainMenuActivity mainMenuActivity;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        mainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);
    }

    /**
     * {@link Test} to assert {@link MainMenuActivity} for not null.
     */
    @Test
    public void nullActivityTest() throws Exception {
        assertNotNull(mainMenuActivity);
    }

    /**
     * {@link Test} to assert title of {@link MainMenuActivity} for not null.
     */
    @Test
    public void titleTest() throws Exception {
        Toolbar toolbar = mainMenuActivity.findViewById(R.id.toolbar);
        assertEquals(mainMenuActivity.getString(R.string.main_menu), toolbar.getTitle());
    }

    /**
     * {@link Test} to assert Options Menu's functioning.
     */
    @Test
    public void optionsMenuTest() throws Exception {
        Menu menu = shadowOf(mainMenuActivity).getOptionsMenu();

        assertNotNull(menu);
        assertNotNull(mainMenuActivity.onCreateOptionsMenu(menu));

        //Test for AboutActivity
        mainMenuActivity.onOptionsItemSelected(menu.getItem(0));
        ShadowActivity shadowActivity = shadowOf(mainMenuActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(AboutActivity.class.getName(), shadowIntent.getIntentClass().getName());

        //Test for About Menu Title
        String menuTitle = mainMenuActivity.getResources().getString(R.string.about_preferences);
        String shadowTitle = menu.getItem(0).getTitle().toString();
        assertEquals(shadowTitle, menuTitle);

        //Test for PreferencesActivity
        mainMenuActivity.onOptionsItemSelected(menu.getItem(1));
        shadowActivity = shadowOf(mainMenuActivity);
        startedIntent = shadowActivity.getNextStartedActivity();
        shadowIntent = shadowOf(startedIntent);
        assertEquals(PreferencesActivity.class.getName(), shadowIntent.getIntentClass().getName());

        //Test for General Settings Menu Title
        menuTitle = mainMenuActivity.getResources().getString(R.string.general_preferences);
        shadowTitle = menu.getItem(1).getTitle().toString();
        assertEquals(shadowTitle, menuTitle);

        //Test for Admin Settings Menu Title
        menuTitle = mainMenuActivity.getResources().getString(R.string.admin_preferences);
        shadowTitle = menu.getItem(2).getTitle().toString();
        assertEquals(shadowTitle, menuTitle);
    }

    /**
     * {@link Test} to assert dataButton's functioning.
     */
    @Test
    public void dataButtonTest() throws Exception {
        Button dataButton = mainMenuActivity.findViewById(R.id.enter_data);

        assertNotNull(dataButton);
        assertEquals(View.VISIBLE, dataButton.getVisibility());
        assertEquals(mainMenuActivity.getString(R.string.enter_data_button), dataButton.getText());

        dataButton.performClick();
        ShadowActivity shadowActivity = shadowOf(mainMenuActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(FormChooserList.class.getName(),
                shadowIntent.getIntentClass().getName());
    }

    /**
     * {@link Test} to assert reviewDataButton's functioning.
     */
    @Test
    public void reviewDataButtonTest() throws Exception {
        Button reviewDataButton = mainMenuActivity.findViewById(R.id.review_data);

        assertNotNull(reviewDataButton);
        assertEquals(View.VISIBLE, reviewDataButton.getVisibility());
        assertEquals(mainMenuActivity.getString(R.string.review_data_button), reviewDataButton.getText());

        reviewDataButton.performClick();
        ShadowActivity shadowActivity = shadowOf(mainMenuActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(InstanceChooserList.class.getName(),
                shadowIntent.getIntentClass().getName());
    }

    /**
     * {@link Test} to assert sendDataButton's functioning.
     */
    @Test
    public void sendDataButtonTest() throws Exception {
        Button sendDataButton = mainMenuActivity.findViewById(R.id.send_data);

        assertNotNull(sendDataButton);
        assertEquals(View.VISIBLE, sendDataButton.getVisibility());
        assertEquals(mainMenuActivity.getString(R.string.send_data_button), sendDataButton.getText());

        sendDataButton.performClick();
        ShadowActivity shadowActivity = shadowOf(mainMenuActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(InstanceUploaderList.class.getName(),
                shadowIntent.getIntentClass().getName());
    }

    /**
     * {@link Test} to assert viewSentFormButton's functioning.
     */
    @Test
    public void viewSentFormButtonTest() throws Exception {
        Button viewSentFormButton = mainMenuActivity.findViewById(R.id.view_sent_forms);

        assertNotNull(viewSentFormButton);
        assertEquals(View.VISIBLE, viewSentFormButton.getVisibility());
        assertEquals(mainMenuActivity.getString(R.string.view_sent_forms), viewSentFormButton.getText());

        viewSentFormButton.performClick();
        ShadowActivity shadowActivity = shadowOf(mainMenuActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(InstanceChooserList.class.getName(),
                shadowIntent.getIntentClass().getName());
    }

    /**
     * {@link Test} to assert getFormButton's functioning.
     */
    @Test
    public void getFormButtonTest() throws Exception {
        Button getFormButton = mainMenuActivity.findViewById(R.id.get_forms);
        assertNotNull(getFormButton);
        assertEquals(View.VISIBLE, getFormButton.getVisibility());
        assertEquals(mainMenuActivity.getString(R.string.get_forms), getFormButton.getText());
    }

    /**
     * {@link Test} to assert manageFilesButton's functioning.
     */
    @Test
    public void manageFilesButtonTest() throws Exception {
        Button manageFilesButton = mainMenuActivity.findViewById(R.id.manage_forms);

        assertNotNull(manageFilesButton);
        assertEquals(View.VISIBLE, manageFilesButton.getVisibility());
        assertEquals(mainMenuActivity.getString(R.string.manage_files), manageFilesButton.getText());

        manageFilesButton.performClick();
        ShadowActivity shadowActivity = shadowOf(mainMenuActivity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(FileManagerTabs.class.getName(),
                shadowIntent.getIntentClass().getName());
    }
}