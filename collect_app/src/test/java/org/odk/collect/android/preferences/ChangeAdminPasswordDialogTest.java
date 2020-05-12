package org.odk.collect.android.preferences;

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
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ChangeAdminPasswordDialogTest {

    private ChangeAdminPasswordDialog dialogFragment;
    private FragmentManager fragmentManager;
    private FragmentActivity activity;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new ChangeAdminPasswordDialog();
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void shouldShowCorrectButtons() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getText(),
                equalTo(activity.getString(R.string.ok)));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(),
                equalTo(activity.getString(R.string.cancel)));
    }

    @Test
    public void clickingOkAfterSettingPassword_callsOnPasswordChanged() {
        TestChangeAdminPasswordDialog fragment = new TestChangeAdminPasswordDialog();

        fragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        passwordEditText.setText("blah");

        assertThat(fragment.password, equals("blah"));
        assertThat(fragment.onPasswordChangedCalled, equalTo(true));
    }

    @Test
    public void passwordIsRetainedOnScreenRotation() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        passwordEditText.setText("blah");

        ChangeAdminPasswordDialog restoredFragment = new ChangeAdminPasswordDialog();
        restoredFragment.show(fragmentManager, "TAG");
        AlertDialog restoredDialog = (AlertDialog) restoredFragment.getDialog();

        assertThat(((EditText) restoredDialog.findViewById(R.id.pwd_field)).getText().toString(), equalTo("blah"));
    }

    @Test
    public void showPasswordCheckBoxValueIsRetainedOnScreenRotation() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        CheckBox passwordCheckBox = dialog.findViewById(R.id.checkBox2);
        Boolean value = !passwordCheckBox.isChecked();
        passwordCheckBox.setChecked(value);

        ChangeAdminPasswordDialog restoredFragment = new ChangeAdminPasswordDialog();
        restoredFragment.show(fragmentManager, "TAG");
        AlertDialog restoredDialog = (AlertDialog) restoredFragment.getDialog();
        assertThat(((CheckBox) restoredDialog.findViewById(R.id.checkBox2)).isChecked(), equalTo(value));
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
        assertThat(passwordEditText.getInputType(), equalTo(InputType.TYPE_CLASS_TEXT));
    }

    @Test
    public void uncheckingShowPassword_displaysPasswordAsPassword() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        CheckBox passwordCheckBox = dialog.findViewById(R.id.checkBox2);

        passwordCheckBox.setChecked(false);
        assertThat(passwordEditText.getInputType(),equalTo(InputType.TYPE_TEXT_VARIATION_PASSWORD));
    }

    private static class TestChangeAdminPasswordDialog extends ChangeAdminPasswordDialog implements ChangeAdminPasswordDialog.ChangePasswordDialogCallback {

        private String password;
        private Boolean onPasswordChangedCalled = false;

        @Override
        public void onPasswordChanged(String password) {
            this.password = password;
            onPasswordChangedCalled = true;
        }
    }
}
