package org.odk.collect.android.activities;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowEnvironment;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowSystemClock;

import java.time.Duration;

import static android.os.Environment.MEDIA_MOUNTED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {
    private MainMenuActivity mainMenuActivity;

    @Before
    public void setUp() throws Exception {
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED);
        mainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);
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
        assertEquals(DeleteSavedFormActivity.class.getName(),
                shadowIntent.getIntentClass().getName());
    }

    @Test
    public void shouldButtonsBeResistantToMulticlicking() {
        ShadowSystemClock.setNanoTime(0);
        assertThat(mainMenuActivity.findViewById(R.id.enter_data).performClick(), is(true));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
        assertThat(mainMenuActivity.findViewById(R.id.enter_data).performClick(), is(false));
        assertThat(mainMenuActivity.findViewById(R.id.review_data).performClick(), is(false));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1001));
        assertThat(mainMenuActivity.findViewById(R.id.enter_data).performClick(), is(true));

        ShadowSystemClock.setNanoTime(0);
        assertThat(mainMenuActivity.findViewById(R.id.review_data).performClick(), is(true));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
        assertThat(mainMenuActivity.findViewById(R.id.review_data).performClick(), is(false));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1001));
        assertThat(mainMenuActivity.findViewById(R.id.review_data).performClick(), is(true));

        ShadowSystemClock.setNanoTime(0);
        assertThat(mainMenuActivity.findViewById(R.id.send_data).performClick(), is(true));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
        assertThat(mainMenuActivity.findViewById(R.id.send_data).performClick(), is(false));
        assertThat(mainMenuActivity.findViewById(R.id.view_sent_forms).performClick(), is(false));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1001));
        assertThat(mainMenuActivity.findViewById(R.id.send_data).performClick(), is(true));

        ShadowSystemClock.setNanoTime(0);
        assertThat(mainMenuActivity.findViewById(R.id.view_sent_forms).performClick(), is(true));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
        assertThat(mainMenuActivity.findViewById(R.id.view_sent_forms).performClick(), is(false));
        assertThat(mainMenuActivity.findViewById(R.id.get_forms).performClick(), is(false));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1001));
        assertThat(mainMenuActivity.findViewById(R.id.view_sent_forms).performClick(), is(true));

        ShadowSystemClock.setNanoTime(0);
        assertThat(mainMenuActivity.findViewById(R.id.get_forms).performClick(), is(true));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
        assertThat(mainMenuActivity.findViewById(R.id.get_forms).performClick(), is(false));
        assertThat(mainMenuActivity.findViewById(R.id.manage_forms).performClick(), is(false));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1001));
        assertThat(mainMenuActivity.findViewById(R.id.get_forms).performClick(), is(true));

        ShadowSystemClock.setNanoTime(0);
        assertThat(mainMenuActivity.findViewById(R.id.manage_forms).performClick(), is(true));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1000));
        assertThat(mainMenuActivity.findViewById(R.id.manage_forms).performClick(), is(false));
        assertThat(mainMenuActivity.findViewById(R.id.enter_data).performClick(), is(false));
        ShadowSystemClock.advanceBy(Duration.ofMillis(1001));
        assertThat(mainMenuActivity.findViewById(R.id.manage_forms).performClick(), is(true));
    }
}