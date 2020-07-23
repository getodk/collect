package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ServerAuthDialogFragmentTest {

    @Test
    public void clickingOK_savesUsernameAndPasswordToGeneralPrefs() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public PreferencesProvider providesPreferencesProvider(Context context) {
                return new PreferencesProvider(context) {
                    @Override
                    public SharedPreferences getGeneralSharedPreferences() {
                        return prefs;
                    }
                };
            }
        });

        FragmentScenario<ServerAuthDialogFragment> scenario = FragmentScenario.launch(
                ServerAuthDialogFragment.class,
                null,
                R.style.Theme_AppCompat,
                null
        );

        scenario.onFragment(fragment -> {
            EditText username = fragment.getDialogView().findViewById(R.id.username_edit);
            EditText password = fragment.getDialogView().findViewById(R.id.password_edit);

            username.setText("Frederick Chilton");
            password.setText("chesapeake");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        });

        assertThat(prefs.getString(GeneralKeys.KEY_USERNAME, null), is("Frederick Chilton"));
        assertThat(prefs.getString(GeneralKeys.KEY_PASSWORD, null), is("chesapeake"));
    }
}