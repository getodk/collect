package org.odk.collect.android.activities;

import android.content.Intent;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowEnvironment;

import static android.os.Environment.MEDIA_MOUNTED;
import static junit.framework.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class MainMenuActivityTest {
    private MainMenuActivity mainMenuActivity;

    /**
     * Runs {@link Before} each test.
     */
    @Before
    public void setUp() throws Exception {
        ShadowEnvironment.setExternalStorageState(MEDIA_MOUNTED);
        mainMenuActivity = Robolectric.setupActivity(MainMenuActivity.class);
    }

    @Test
    public void pressingConfigureQRCode_launchesScanQRCodeActivity() throws Exception {
        MenuItem item = new RoboMenuItem(R.id.menu_configure_qr_code);

        mainMenuActivity.onOptionsItemSelected(item);

        Intent expectedIntent = new Intent(mainMenuActivity, ScanQRCodeActivity.class);
        Intent actual = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}
