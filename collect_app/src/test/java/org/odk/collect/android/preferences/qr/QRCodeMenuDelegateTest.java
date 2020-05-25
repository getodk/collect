package org.odk.collect.android.preferences.qr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import com.google.zxing.WriterException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.utilities.Consumer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.io.IOException;
import java.util.Collection;

import io.reactivex.Observable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class QRCodeMenuDelegateTest {

    private final ActivityAvailability activityAvailability = mock(ActivityAvailability.class);
    private final FakeQRCodeGenerator qrCodeGenerator = new FakeQRCodeGenerator();
    private final FileProvider fileProvider = mock(FileProvider.class);

    private Activity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).get();
    }

    @Test
    public void clickingOnImportQRCode_startsExternalImagePickerIntent() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(true);

        QRCodeMenuDelegate menuDelegate = new QRCodeMenuDelegate(activity, activityAvailability, qrCodeGenerator, fileProvider);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_scan_sd_card));

        ShadowActivity.IntentForResult intentForResult = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(intentForResult, notNullValue());
        assertThat(intentForResult.intent.getAction(), is(Intent.ACTION_PICK));
        assertThat(intentForResult.intent.getType(), is("image/*"));
    }

    @Test
    public void clickingOnImportQRCode_whenPickerActivityNotAvailable_showsToast() {
        when(activityAvailability.isActivityAvailable(any())).thenReturn(false);

        QRCodeMenuDelegate menuDelegate = new QRCodeMenuDelegate(activity, activityAvailability, qrCodeGenerator, fileProvider);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_scan_sd_card));

        assertThat(shadowOf(activity).getNextStartedActivityForResult(), is(nullValue()));
        assertThat(ShadowToast.getLatestToast(), notNullValue());
    }

    @Test
    public void clickingOnShare_startsShareIntentWhenQRCodeGenerated() {
        when(fileProvider.getURIForFile("qr.png")).thenReturn(Uri.parse("uri"));

        QRCodeMenuDelegate menuDelegate = new QRCodeMenuDelegate(activity, activityAvailability, qrCodeGenerator, fileProvider);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_item_share));
        assertThat(shadowOf(activity).getNextStartedActivity(), is(nullValue()));

        qrCodeGenerator.generate("qr.png");

        Intent intent = shadowOf(activity).getNextStartedActivity();
        assertThat(intent, notNullValue());
        assertThat(intent.getAction(), is(Intent.ACTION_SEND));
        assertThat(intent.getType(), is("image/*"));
        assertThat(intent.getExtras().getParcelable(Intent.EXTRA_STREAM), is(Uri.parse("uri")));
    }

    private static class FakeQRCodeGenerator implements QRCodeGenerator {

        private Consumer<String> callback;

        @Override
        public void generateQRCode(Consumer<String> callback) {
            this.callback = callback;
        }

        @Override
        public Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys) {
            return null;
        }

        @Override
        public Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException {
            return null;
        }

        @Override
        public String getQRCodeFilepath() {
            return null;
        }

        @Override
        public String getMd5CachePath() {
            return null;
        }

        public void generate(String path) {
            callback.accept(path);
        }
    }
}