/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.odk.collect.android.R;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.permissions.PermissionsProvider;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.analytics.AnalyticsEvents.SHOW_SPLASH_SCREEN;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_SPLASH_PATH;

public class SplashScreenActivity extends Activity {

    private static final int SPLASH_TIMEOUT = 2000; // milliseconds

    private int imageMaxWidth;

    @Inject
    Analytics analytics;

    @Inject
    PermissionsProvider permissionsProvider;

    @Inject
    SettingsProvider settingsProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this splash screen should be a blank slate
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DaggerUtils.getComponent(this).inject(this);
        init();
    }

    private void init() {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        imageMaxWidth = displayMetrics.widthPixels;

        setContentView(R.layout.splash_screen);

        boolean showSplash = settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_SHOW_SPLASH);
        String splashPath = settingsProvider.getGeneralSettings().getString(KEY_SPLASH_PATH);

        if (showSplash) {
            startSplashScreen(splashPath);

            String splashPathHash = FileUtils.getMd5Hash(new ByteArrayInputStream(splashPath.getBytes()));
            analytics.logEvent(SHOW_SPLASH_SCREEN, splashPathHash, "");
        } else {
            endSplashScreen();
        }
    }

    private void endSplashScreen() {
        startActivity(new Intent(this, MainMenuActivity.class));
        finish();
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Timber.e(e, "Unable to close file input stream");
            }

            int scale = 1;
            if (o.outHeight > imageMaxWidth || o.outWidth > imageMaxWidth) {
                scale =
                        (int) Math.pow(
                                2,
                                (int) Math.round(Math.log(imageMaxWidth
                                        / (double) Math.max(o.outHeight, o.outWidth))
                                        / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            try {
                fis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Timber.e(e, "Unable to close file input stream");
            }
        } catch (FileNotFoundException e) {
            Timber.d(e);
        }
        return b;
    }

    private void startSplashScreen(String path) {

        // add items to the splash screen here. makes things less distracting.
        ImageView customSplashView = findViewById(R.id.splash);
        LinearLayout defaultSplashView = findViewById(R.id.splash_default);

        File customSplash = new File(path);
        if (customSplash.exists()) {
            customSplashView.setImageBitmap(decodeFile(customSplash));
            defaultSplashView.setVisibility(View.GONE);
            customSplashView.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(this::endSplashScreen, SPLASH_TIMEOUT);
    }
}
