package org.odk.collect.android.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.odk.collect.android.database.ActivityLogger;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * @author James Knight
 */

public abstract class InjectableActivity extends AppCompatActivity {

    @Inject
    protected ActivityLogger activityLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityLogger.logOnStart(this);
    }

    @Override
    protected void onStop() {
        activityLogger.logOnStop(this);
        super.onStop();
    }
}
