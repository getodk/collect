package org.odk.collect.android.preferences;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestActivityScenario;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowDialog;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ChangeAdminPasswordDialogTest {

    private TestActivityScenario<DialogFragmentTestActivity> activityScenario;
    private ActivityController<FragmentActivity> activity;
    private FragmentManager fragmentManager;
    private ChangeAdminPasswordDialog dialogFragment;
    private ChangeAdminPasswordDialog.ChangePasswordDialogCallback callback;

    @Before
    public void setup() {
        activityScenario = TestActivityScenario.launch(DialogFragmentTestActivity.class);
        activity = RobolectricHelpers.buildThemedActivity(FragmentActivity.class);
        activity.setup();

        fragmentManager = activity.get().getSupportFragmentManager();
        dialogFragment = new ChangeAdminPasswordDialog();

        callback = mock(ChangeAdminPasswordDialog.ChangePasswordDialogCallback.class);
        dialogFragment.callback = callback;
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "TAG");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void clickingOkAfterSettingPassword_callsOnPasswordChanged() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        EditText passwordEditText = dialog.findViewById(R.id.pwd_field);
        passwordEditText.setText("blah");
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        verify(callback, times(1)).onPasswordChanged("blah");
    }

    @Test
    public void whenScreenIsRotated_passwordAndCheckboxValueIsRetained() {
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

    private static class DialogFragmentTestActivity extends FragmentActivity {

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(R.style.Theme_AppCompat); // Needed for androidx.appcompat.app.AlertDialog
        }
    }
}
