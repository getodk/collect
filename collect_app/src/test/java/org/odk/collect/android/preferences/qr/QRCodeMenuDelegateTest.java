package org.odk.collect.android.preferences.qr;

import android.app.Activity;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class QRCodeMenuDelegateTest {

    private final ActivityAvailability activityAvailability = mock(ActivityAvailability.class);
    private Activity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).get();
    }

    @Test
    public void clickingOnImportQRCode_whenPickerActivityAvailable_startsExternalImagePickerIntent() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);

        QRCodeMenuDelegate menuDelegate = new QRCodeMenuDelegate(activity, activityAvailability);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_scan_sd_card));

        ShadowActivity.IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(intentForResult, notNullValue());
        assertThat(intentForResult.intent.getAction(), is(Intent.ACTION_PICK));
        assertThat(intentForResult.intent.getType(), is("image/*"));
    }

    @Test
    public void clickingOnImportQRCode_whenPickerActivityNotAvailable_showsToast() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(false);

        QRCodeMenuDelegate menuDelegate = new QRCodeMenuDelegate(activity, activityAvailability);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_scan_sd_card));

        assertThat(shadowOf(activity).getNextStartedActivityForResult(), is(nullValue()));
        assertThat(ShadowToast.getLatestToast(), notNullValue());
    }
}