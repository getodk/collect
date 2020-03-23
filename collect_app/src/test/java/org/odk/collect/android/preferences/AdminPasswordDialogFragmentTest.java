package org.odk.collect.android.preferences;

import android.app.AlertDialog;

import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestActivityScenario;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.robolectric.annotation.LooperMode;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.preferences.AdminPasswordDialogFragment.Action.STORAGE_MIGRATION;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class AdminPasswordDialogFragmentTest {

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public AdminPasswordProvider providesAdminPasswordProvider() {
                return new StubAdminPasswordProvider();
            }
        });
    }

    @Test
    public void enteringPassword_andClickingOK_callsOnCorrectAdminPasswordWithAction() {
        TestActivityScenario<MockAdminPasswordDialogCallback> activityScenario = TestActivityScenario.launch(MockAdminPasswordDialogCallback.class);
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = new AdminPasswordDialogFragment();
            fragment.setAction(STORAGE_MIGRATION);
            fragment.show(activity.getSupportFragmentManager(), "tag");
            shadowOf(getMainLooper()).idle();

            fragment.getInput().setText("password");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            shadowOf(getMainLooper()).idle();

            assertThat(activity.onCorrectAdminPasswordCalledWith, equalTo(STORAGE_MIGRATION));
        });
    }

    @Test
    public void afterRecreating_enteringPassword_andClickingOK_callsOnCorrectAdminPasswordWithAction() {
        TestActivityScenario<MockAdminPasswordDialogCallback> activityScenario = TestActivityScenario.launch(MockAdminPasswordDialogCallback.class);
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = new AdminPasswordDialogFragment();
            fragment.setAction(STORAGE_MIGRATION);
            fragment.show(activity.getSupportFragmentManager(), "tag");
            shadowOf(getMainLooper()).idle();
        });

        activityScenario.recreate();
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = (AdminPasswordDialogFragment) activity.getSupportFragmentManager().findFragmentByTag("tag");

            fragment.getInput().setText("password");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            shadowOf(getMainLooper()).idle();

            assertThat(activity.onCorrectAdminPasswordCalledWith, equalTo(STORAGE_MIGRATION));
        });
    }

    private static class StubAdminPasswordProvider extends AdminPasswordProvider {

        StubAdminPasswordProvider() {
            super(null);
        }

        @Override
        public boolean isAdminPasswordSet() {
            return true;
        }

        @Override
        public String getAdminPassword() {
            return "password";
        }
    }

    private static class MockAdminPasswordDialogCallback extends FragmentActivity implements AdminPasswordDialogFragment.AdminPasswordDialogCallback  {

        private AdminPasswordDialogFragment.Action onCorrectAdminPasswordCalledWith;

        @Override
        public void onCorrectAdminPassword(AdminPasswordDialogFragment.Action action) {
            onCorrectAdminPasswordCalledWith = action;
        }

        @Override
        public void onIncorrectAdminPassword() {

        }
    }
}