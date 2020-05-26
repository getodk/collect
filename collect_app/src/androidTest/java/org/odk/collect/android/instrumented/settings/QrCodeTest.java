/*
 * Copyright 2018 Shobhit Agarwal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.instrumented.settings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceSaver;
import org.odk.collect.android.preferences.qr.CachingQRCodeGenerator;
import org.odk.collect.android.preferences.qr.QRCodeGenerator;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QRCodeUtils;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.preferences.GeneralKeys.DEFAULTS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_SELECTED_GOOGLE_ACCOUNT;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_SHOW_SPLASH;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_USERNAME;

@RunWith(AndroidJUnit4.class)
public class QrCodeTest {

    private final GeneralSharedPreferences preferences = GeneralSharedPreferences.getInstance();
    private final QRCodeGenerator qrCodeGenerator = new CachingQRCodeGenerator();

    @Test
    public void importSettingsFromQrCode() throws Exception {
        // reset preferences
        preferences.loadDefaultPreferences();

        // verify that the following preferences actually have default values
        String[] keys = {KEY_USERNAME, KEY_SELECTED_GOOGLE_ACCOUNT, KEY_SHOW_SPLASH};
        assertPreferenceHaveDefaultValue(keys, true);

        // updating the preferences
        preferences
                .save(KEY_USERNAME, "test_username")
                .save(KEY_SELECTED_GOOGLE_ACCOUNT, "test@email.com")
                .save(KEY_SHOW_SPLASH, true);

        // verify that preferences values have been modified
        assertPreferenceHaveDefaultValue(keys, false);

        // generate and fetch QrCode
        String filePath = qrCodeGenerator.generateQRCode(new ArrayList<>());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap qrCode = FileUtils.getBitmap(filePath, options);

        assertNotNull(qrCode);

        // reset preferences
        preferences.loadDefaultPreferences();

        // verify again that preferences are actually reset to default
        assertPreferenceHaveDefaultValue(keys, true);

        // decode the generated bitmap
        String result = QRCodeUtils.decodeFromBitmap(qrCode);
        assertNotNull(result);

        String resultIfAllSharedPreferencesAreDefault = "{\"general\":{},\"admin\":{}}";
        assertNotEquals(resultIfAllSharedPreferencesAreDefault, result);

        // update shared preferences using the QrCode
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(result, null);

        // assert that values have updated properly
        assertPreferenceHaveDefaultValue(keys, false);
        assertEquals("test_username", preferences.get(keys[0]));
        assertEquals("test@email.com", preferences.get(keys[1]));
        assertTrue((Boolean) preferences.get(keys[2]));
    }

    private void assertPreferenceHaveDefaultValue(String[] keys, boolean shouldBeDefault) {
        for (String key : keys) {
            if (shouldBeDefault) {
                assertEquals(DEFAULTS.get(key), preferences.get(key));
            } else {
                assertNotEquals(DEFAULTS.get(key), preferences.get(key));
            }
        }
    }
}
