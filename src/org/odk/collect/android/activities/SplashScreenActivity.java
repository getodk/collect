
package org.odk.collect.android.activities;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferencesActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashScreenActivity extends Activity {

    @SuppressWarnings("rawtypes")
    private Class mNextActivity = MainMenuActivity.class;
    private int mSplashTimeout = 3000; // milliseconds

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this splash screen should be a blank slate
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_screen);

        // get the shared preferences object
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();

        // get the package info object with version number
        PackageInfo packageInfo = null;
        try {
            packageInfo =
                getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        boolean firstRun = sharedPreferences.getBoolean(PreferencesActivity.KEY_FIRST_RUN, true);
        boolean showSplash = sharedPreferences.getBoolean(PreferencesActivity.KEY_SHOW_SPLASH, false);

        boolean versionChange;
        
        // if you've increased version code, then update the version number and set firstRun to true
        if (sharedPreferences.getLong(PreferencesActivity.KEY_LAST_VERSION, 0) < packageInfo.versionCode) {
            editor.putLong(PreferencesActivity.KEY_LAST_VERSION, packageInfo.versionCode);
            editor.commit();

            versionChange = true;
            firstRun = true;
        }

        // do all the first run things
        if (firstRun || showSplash) {
            editor.putBoolean(PreferencesActivity.KEY_FIRST_RUN, false);
            editor.commit();
            startSplashScreen();
        } else {
            endSplashScreen();
        }

    }


    private void endSplashScreen() {

        // launch new activity and close splash screen
        startActivity(new Intent(SplashScreenActivity.this, mNextActivity));
        finish();
    }


    private void startSplashScreen() {

        // add items to the splash screen here. makes things less distracting.
        LinearLayout ll = (LinearLayout) findViewById(R.id.layout);
        ll.setBackgroundColor(Color.RED);
        TextView tv = (TextView) findViewById(R.id.text);
        tv.setText("ODK Collect");

        // create a thread that counts up to the timeout
        Thread t = new Thread() {
            int count = 0;

            @Override
            public void run() {
                try {
                    super.run();
                    while (count < mSplashTimeout) {
                        sleep(100);
                        count += 100;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endSplashScreen();
                }
            }
        };
        t.start();
    }

}
