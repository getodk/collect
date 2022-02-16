package org.odk.collect.android.configure.qr;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.configure.qr.QRCodeMenuDelegate.SELECT_PHOTO;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowToast.getTextOfLatestToast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.projects.Project;
import org.odk.collect.settings.ODKAppSettingsImporter;
import org.robolectric.Robolectric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class QRCodeActivityResultDelegateTest {
    private final FakeQRDecoder fakeQRDecoder = new FakeQRDecoder();
    private final ODKAppSettingsImporter settingsImporter = mock(ODKAppSettingsImporter.class);
    private final Project.Saved project = mock(Project.Saved.class);
    private Activity context;

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public QRCodeDecoder providesQRCodeDecoder() {
                return fakeQRDecoder;
            }
        });

        context = Robolectric.buildActivity(Activity.class).get();
    }

    @Test
    public void forSelectPhoto_importsSettingsFromQRCode_showsSuccessToast() {
        importSettingsFromQRCode_successfully();
        assertThat(getTextOfLatestToast(), is(context.getString(R.string.successfully_imported_settings)));
    }

    private void importSettingsFromQRCode_successfully() {
        QRCodeActivityResultDelegate delegate = new QRCodeActivityResultDelegate(context, settingsImporter, fakeQRDecoder, project);

        Intent data = intentWithData("file://qr", "qr");
        fakeQRDecoder.register("qr", "data");
        when(settingsImporter.fromJSON("data", project)).thenReturn(true);

        delegate.onActivityResult(SELECT_PHOTO, Activity.RESULT_OK, data);
    }

    @Test
    public void forSelectPhoto_whenImportingFails_showsInvalidToast() {
        importSettingsFromQRCode_withFailedImport();
        assertThat(getTextOfLatestToast(), is(context.getString(R.string.invalid_qrcode)));
    }

    private void importSettingsFromQRCode_withFailedImport() {
        QRCodeActivityResultDelegate delegate = new QRCodeActivityResultDelegate(context, settingsImporter, fakeQRDecoder, project);

        Intent data = intentWithData("file://qr", "qr");
        fakeQRDecoder.register("qr", "data");
        when(settingsImporter.fromJSON("data", project)).thenReturn(false);

        delegate.onActivityResult(SELECT_PHOTO, Activity.RESULT_OK, data);
    }

    @Test
    public void forSelectPhoto_whenQRCodeDecodeFailsWithInvalid_showsInvalidToast() {
        importSettingsFromQrCode_withInvalidQrCode();
        assertThat(getTextOfLatestToast(), is(context.getString(R.string.invalid_qrcode)));
    }

    private void importSettingsFromQrCode_withInvalidQrCode() {
        QRCodeActivityResultDelegate delegate = new QRCodeActivityResultDelegate(context, settingsImporter, fakeQRDecoder, project);

        Intent data = intentWithData("file://qr", "qr");
        fakeQRDecoder.failsWith(new QRCodeDecoder.InvalidException());
        when(settingsImporter.fromJSON("data", project)).thenReturn(false);

        delegate.onActivityResult(SELECT_PHOTO, Activity.RESULT_OK, data);
    }

    @Test
    public void forSelectPhoto_whenQRCodeDecodeFailsWithNotFound_showsNoQRToast() {
        importSettingsFromImage_withoutQrCode();
        assertThat(getTextOfLatestToast(), is(context.getString(R.string.qr_code_not_found)));
    }

    private void importSettingsFromImage_withoutQrCode() {
        QRCodeActivityResultDelegate delegate = new QRCodeActivityResultDelegate(context, settingsImporter, fakeQRDecoder, project);

        Intent data = intentWithData("file://qr", "qr");
        fakeQRDecoder.failsWith(new QRCodeDecoder.NotFoundException());
        when(settingsImporter.fromJSON("data", project)).thenReturn(false);

        delegate.onActivityResult(SELECT_PHOTO, Activity.RESULT_OK, data);
    }

    @Test
    public void forSelectPhoto_whenDataIsNull_doesNothing() {
        QRCodeActivityResultDelegate delegate = new QRCodeActivityResultDelegate(context, settingsImporter, fakeQRDecoder, project);
        delegate.onActivityResult(SELECT_PHOTO, Activity.RESULT_OK, null);
    }

    @Test
    public void forSelectPhoto_whenResultCancelled_doesNothing() {
        QRCodeActivityResultDelegate delegate = new QRCodeActivityResultDelegate(context, settingsImporter, fakeQRDecoder, project);
        delegate.onActivityResult(SELECT_PHOTO, Activity.RESULT_CANCELED, new Intent());
    }

    @NotNull
    private Intent intentWithData(String uri, String streamContents) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(streamContents.getBytes());
        shadowOf(getApplicationContext().getContentResolver()).registerInputStream(Uri.parse("file://qr"), inputStream);

        Intent data = new Intent();
        data.setData(Uri.parse(uri));
        return data;
    }

    private static class FakeQRDecoder implements QRCodeDecoder {

        private final Map<String, String> data = new HashMap<>();
        private Exception failsWith;

        @Override
        public String decode(InputStream inputStream) throws InvalidException, NotFoundException {
            if (failsWith != null) {
                if (failsWith instanceof InvalidException) {
                    throw (InvalidException) failsWith;
                } else {
                    throw (NotFoundException) failsWith;
                }
            }

            try {
                String streamData = IOUtils.toString(inputStream);
                String decoded = data.get(streamData);

                if (decoded == null) {
                    throw new RuntimeException("No decoded data specified for " + streamData);
                }
                return decoded;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void register(String streamData, String decodedData) {
            data.put(streamData, decodedData);
        }

        public void failsWith(Exception exception) {
            failsWith = exception;
        }
    }
}
