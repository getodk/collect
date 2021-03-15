package org.odk.collect.android.preferences.dialogs;

import android.content.DialogInterface;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.fragments.support.DialogFragmentHelpers;
import org.odk.collect.android.preferences.source.Settings;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestActivityScenario;
import org.odk.collect.utilities.TestSettingsProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_ADMIN_PW;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ChangeAdminPasswordDialogTest {

    private FragmentManager fragmentManager;
    private ChangeAdminPasswordDialog dialogFragment;
    private final Settings adminSettings = TestSettingsProvider.getAdminSettings();

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);

        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new ChangeAdminPasswordDialog();
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void clickingOkAfterSettingPassword_setsPasswordInSharedPreferences() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        passwordEditText.setText("blah");
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        assertThat(adminSettings.getString(KEY_ADMIN_PW), equalTo("blah"));
    }

    @Test
    public void whenScreenIsRotated_passwordAndCheckboxValueIsRetained() {
        TestActivityScenario<DialogFragmentHelpers.DialogFragmentTestActivity> activityScenario = TestActivityScenario
                .launch(DialogFragmentHelpers.DialogFragmentTestActivity.class);
        activityScenario.onActivity(activity -> {
            dialogFragment.show(activity.getSupportFragmentManager(), "TAG");
            AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
            ((EditText) dialog.findViewById(R.id.pwd_field)).setText("blah");
            ((CheckBox) dialog.findViewById(R.id.checkBox2)).setChecked(true);
        });

        activityScenario.recreate();

        activityScenario.onActivity(activity -> {
            ChangeAdminPasswordDialog restoredFragment = (ChangeAdminPasswordDialog) activity.getSupportFragmentManager().findFragmentByTag("TAG");
            AlertDialog restoredDialog = (AlertDialog) restoredFragment.getDialog();
            assertThat(((EditText) restoredDialog.findViewById(R.id.pwd_field)).getText().toString(), equalTo("blah"));
            assertThat(((CheckBox) restoredDialog.findViewById(R.id.checkBox2)).isChecked(), equalTo(true));
        });
    }

    @Test
    public void clickingOk_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void clickingCancel_dismissesTheDialog() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }

    @Test
    public void checkingShowPassword_displaysPasswordAsText() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        CheckBox passwordCheckBox = dialog.findViewById(R.id.checkBox2);

        passwordCheckBox.setChecked(true);
        assertThat(passwordEditText.getInputType(), equalTo(InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    @Test
    public void uncheckingShowPassword_displaysPasswordAsPassword() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        CheckBox passwordCheckBox = dialog.findViewById(R.id.checkBox2);

        passwordCheckBox.setChecked(false);
        assertThat(passwordEditText.getInputType(), equalTo(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }
}
