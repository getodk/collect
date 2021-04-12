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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ScreenUtils;

import java.io.File;

import javax.inject.Inject;

import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_SPLASH_PATH;

public class SplashScreenActivity extends Activity {

    private static final int SPLASH_TIMEOUT = 2000; // milliseconds

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
        setContentView(R.layout.splash_screen);

        if (settingsProvider.getGeneralSettings().getBoolean(GeneralKeys.KEY_SHOW_SPLASH)) {
            startSplashScreen(settingsProvider.getGeneralSettings().getString(KEY_SPLASH_PATH));
        } else {
            endSplashScreen();
        }
    }

    private void endSplashScreen() {
        startActivityAndCloseAllOthers(this, MainMenuActivity.class);
    }

    private void startSplashScreen(String path) {
        // add items to the splash screen here. makes things less distracting.
        ImageView customSplashView = findViewById(R.id.splash);
        LinearLayout defaultSplashView = findViewById(R.id.splash_default);

        File customSplash = new File(path);
        if (customSplash.exists()) {
            customSplashView.setImageBitmap(FileUtils.getBitmapScaledToDisplay(customSplash, ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth()));
            defaultSplashView.setVisibility(View.GONE);
            customSplashView.setVisibility(View.VISIBLE);
        }

        new Handler().postDelayed(this::endSplashScreen, SPLASH_TIMEOUT);
    }
}
