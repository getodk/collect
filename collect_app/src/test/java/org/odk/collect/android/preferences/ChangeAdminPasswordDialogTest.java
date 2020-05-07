package org.odk.collect.android.preferences;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.VISIBLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(RobolectricTestRunner.class)
public class ChangeAdminPasswordDialogTest {

    private ChangeAdminPasswordDialog dialogFragment;
    private FragmentManager fragmentManager;
    private FragmentActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.setupActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new ChangeAdminPasswordDialog();
    }

    @Test
    public void dialogIsCancellable() {

    }

    @Test
    public void shouldShowCorrectButtons() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(),
                equalTo(activity.getString(R.string.cancel)));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(),
                equalTo(activity.getString(R.string.ok)));
    }

    @Test
    public void clickingOkAfterEnteringPassword_callsOnPasswordChanged() {

    }

    @Test
    public void clickingOkWithoutEnteringPassword_callsOnEmptyPasswordSubmitted() {

    }

    @Test
    public void passwordIsRetainedOnScreenRotation() {

    }

    @Test
    public void clickingOk_dismissesTheDialog() {

    }

    @Test
    public void clickingCancel_dismissesTheDialog() {

    }

    @Test
    public void checkingShowPasswordBeforeEnteringPassword_displaysPasswordAsText() {

    }

    @Test
    public void uncheckingShowPasswordBeforeEnteringPassword_displaysPasswordAsPassword() {

    }

    @Test
    public void checkingShowPasswordAfterEnteringPassword_displaysPasswordAsText() {

    }

    @Test
    public void uncheckingShowPasswordAfterEnteringPassword_displaysPasswordAsPassword() {

    }
}
