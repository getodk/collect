package org.odk.collect.android.preferences;

import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.utilities.TestPreferencesProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ServerAuthDialogFragmentTest {

    private final PreferencesDataSource generalPrefs = TestPreferencesProvider.getGeneralPreferences();

    @Before
    public void setup() {
        generalPrefs.save(GeneralKeys.KEY_USERNAME, "Alpen");
        generalPrefs.save(GeneralKeys.KEY_PASSWORD, "swiss");
    }

    @Test
    public void prefillsUsernameAndPassword() {
        generalPrefs.save(GeneralKeys.KEY_USERNAME, "Alpen");
        generalPrefs.save(GeneralKeys.KEY_PASSWORD, "swiss");

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

        assertThat(generalPrefs.getString(GeneralKeys.KEY_USERNAME), is("Frederick Chilton"));
        assertThat(generalPrefs.getString(GeneralKeys.KEY_PASSWORD), is("chesapeake"));
    }
}
