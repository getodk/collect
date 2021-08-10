package org.odk.collect.android.configure.qr;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.testshared.FakeScheduler;
import org.robolectric.Robolectric;
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
import static org.odk.collect.android.configure.qr.QRCodeMenuDelegate.SELECT_PHOTO;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class QRCodeMenuDelegateTest {

    private final ActivityAvailability activityAvailability = mock(ActivityAvailability.class);
    private final QRCodeGenerator qrCodeGenerator = mock(QRCodeGenerator.class);
    private final AppConfigurationGenerator appConfigurationGenerator = mock(AppConfigurationGenerator.class);
    private final FileProvider fileProvider = mock(FileProvider.class);
    private final FakeScheduler fakeScheduler = new FakeScheduler();

    private FragmentActivity activity;
    private QRCodeMenuDelegate menuDelegate;

    @Before
    public void setup() {
        activity = Robolectric.setupActivity(FragmentActivity.class);
        menuDelegate = new QRCodeMenuDelegate(activity, activityAvailability, qrCodeGenerator,
                appConfigurationGenerator, fileProvider, TestSettingsProvider.getSettingsProvider(), fakeScheduler);
    }

    @Test
    public void clickingOnImportQRCode_startsExternalImagePickerIntent() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_scan_sd_card));

        ShadowActivity.IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(intentForResult, notNullValue());
        assertThat(intentForResult.requestCode, is(SELECT_PHOTO));
        assertThat(intentForResult.intent.getAction(), is(Intent.ACTION_GET_CONTENT));
        assertThat(intentForResult.intent.getType(), is("image/*"));
    }

    @Test
    public void clickingOnImportQRCode_whenPickerActivityNotAvailable_showsToast() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(false);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_scan_sd_card));

        assertThat(shadowOf(activity).getNextStartedActivityForResult(), is(nullValue()));
        assertThat(ShadowToast.getLatestToast(), notNullValue());
    }

    @Test
    public void clickingOnShare_beforeQRCodeIsGenerated_doesNothing() throws Exception {
        when(qrCodeGenerator.generateQRCode(any(), any())).thenReturn("qr.png");
        when(fileProvider.getURIForFile("qr.png")).thenReturn(Uri.parse("uri"));

        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_share));

        assertThat(shadowOf(activity).getNextStartedActivity(), is(nullValue()));
    }

    @Test
    public void clickingOnShare_whenQRCodeIsGenerated_startsShareIntent() throws Exception {
        when(qrCodeGenerator.generateQRCode(any(), any())).thenReturn("qr.png");
        when(fileProvider.getURIForFile("qr.png")).thenReturn(Uri.parse("uri"));
        fakeScheduler.runBackground();

        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_share));

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertThat(intent, notNullValue());
        assertThat(intent.getAction(), is(Intent.ACTION_SEND));
        assertThat(intent.getType(), is("image/*"));
        assertThat(intent.getExtras().getParcelable(Intent.EXTRA_STREAM), is(Uri.parse("uri")));
    }
}