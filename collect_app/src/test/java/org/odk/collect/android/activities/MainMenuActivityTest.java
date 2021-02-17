package org.odk.collect.android.activities;

import android.content.Intent;
import android.view.MenuItem;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LooperMode;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowEnvironment;

import static android.os.Environment.MEDIA_MOUNTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class MainMenuActivityTest {

    @Before
    public void setUp() throws Exception {
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED); // Required for ODK directories to be created
    }

    @Test
    public void pressingConfigureQRCode_launchesQRCodeTabsActivity() {
        ActivityScenario<MainMenuActivity> firstActivity = ActivityScenario.launch(MainMenuActivity.class);
        firstActivity.onActivity(activity -> {
            MenuItem item = new RoboMenuItem(R.id.menu_configure_qr_code);
            activity.onOptionsItemSelected(item);

            Intent expectedIntent = new Intent(activity, QRCodeTabsActivity.class);
            Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
            assertThat(expectedIntent.getComponent(), equalTo(actual.getComponent()));
        });
    }
}
