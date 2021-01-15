package org.odk.collect.android.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
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

    private SharedPreferences generalPrefs;

    @Before
    public void setup() {
        generalPrefs = getApplicationContext().getSharedPreferences("test", Context.MODE_PRIVATE);
        generalPrefs.edit()
                .putString(GeneralKeys.KEY_USERNAME, "Alpen")
                .putString(GeneralKeys.KEY_PASSWORD, "swiss")
                .apply();

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public PreferencesProvider providesPreferencesProvider(Context context) {
                return new PreferencesProvider(context) {
                    @Override
                    public SharedPreferences getGeneralSharedPreferences() {
                        return generalPrefs;
                    }
                };
            }
        });
    }

    @Test
    public void prefillsUsernameAndPassword() {
        generalPrefs.edit()
                .putString(GeneralKeys.KEY_USERNAME, "Alpen")
                .putString(GeneralKeys.KEY_PASSWORD, "swiss")
                .apply();

        FragmentScenario<ServerAuthDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(ServerAuthDialogFragment.class);

        scenario.onFragment(fragment -> {
            EditText username = fragment.getDialogView().findViewById(R.id.username_edit);
            EditText password = fragment.getDialogView().findViewById(R.id.password_edit);

            assertThat(username.getText().toString(), is("Alpen"));
            assertThat(password.getText().toString(), is("swiss"));
        });
    }

    @Test
    public void clickingOK_savesUsernameAndPasswordToGeneralPrefs() {
        FragmentScenario<ServerAuthDialogFragment> scenario = RobolectricHelpers.launchDialogFragment(ServerAuthDialogFragment.class);

        scenario.onFragment(fragment -> {
            EditText username = fragment.getDialogView().findViewById(R.id.username_edit);
            EditText password = fragment.getDialogView().findViewById(R.id.password_edit);

            username.setText("Frederick Chilton");
            password.setText("chesapeake");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        });

        assertThat(generalPrefs.getString(GeneralKeys.KEY_USERNAME, null), is("Frederick Chilton"));
        assertThat(generalPrefs.getString(GeneralKeys.KEY_PASSWORD, null), is("chesapeake"));
    }
}
