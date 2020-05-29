package org.odk.collect.android.preferences.qr;


import android.content.SharedPreferences;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

@RunWith(AndroidJUnit4.class)
public class QRCodeViewModelTest {

    private QRCodeGenerator qrCodeGenerator;
    private SharedPreferences preferences;

    @Before
    public void setup() {
        qrCodeGenerator = mock(QRCodeGenerator.class);
        preferences = getDefaultSharedPreferences(getApplicationContext());
    }

    @Test
    public void setIncludedKeys_generatesQRCodeWithKeys() throws Exception {
        QRCodeViewModel viewModel = new QRCodeViewModel(qrCodeGenerator, preferences, preferences);
        viewModel.setIncludedKeys(asList("foo", "bar"));
        verify(qrCodeGenerator).generateQRCode(asList("foo", "bar"));
    }

    @Test
    public void warning_whenNeitherServerOrAdminPasswordSet_isNull() {
        QRCodeViewModel viewModel = new QRCodeViewModel(qrCodeGenerator, preferences, preferences);
        assertThat(viewModel.getWarning().getValue(), is(nullValue()));
    }

    @Test
    public void warning_whenServerAndAdminPasswordSet_isForBoth() {
        preferences.edit()
                .putString(KEY_PASSWORD, "blah")
                .putString(KEY_ADMIN_PW, "blah")
                .apply();

        QRCodeViewModel viewModel = new QRCodeViewModel(qrCodeGenerator, preferences, preferences);
        assertThat(viewModel.getWarning().getValue(), is(R.string.qrcode_with_both_passwords));
    }
}