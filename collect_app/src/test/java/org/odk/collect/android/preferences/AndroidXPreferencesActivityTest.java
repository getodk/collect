package org.odk.collect.android.preferences;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.odk.collect.android.preferences.AndroidXPreferencesActivity.EXTRA_FRAGMENT_FORM_METADATA;
import static org.odk.collect.android.preferences.AndroidXPreferencesActivity.KEY_EXTRA_FRAGMENT;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class AndroidXPreferencesActivityTest {

    @Test
    public void whenRecreated_fragmentsAreNotReplacedWithNewOnes() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AndroidXPreferencesActivity.class);
        intent.putExtra(KEY_EXTRA_FRAGMENT, EXTRA_FRAGMENT_FORM_METADATA);
        ActivityScenario<AndroidXPreferencesActivity> scenario = ActivityScenario.launch(intent);
        scenario.onActivity(activity -> {
            Bundle args = new Bundle();
            args.putBoolean("RETAINED", true);
            activity.getSupportFragmentManager().getFragments().get(0).setArguments(args);
        });

        scenario.recreate();
        scenario.onActivity(activity -> {
            Fragment fragment = activity.getSupportFragmentManager().getFragments().get(0);
            assertThat(fragment.getArguments().getBoolean("RETAINED"), is(true));
        });
    }
}