package org.odk.collect.android.preferences;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestActivityScenario;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.robolectric.annotation.LooperMode;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.odk.collect.android.preferences.AdminPasswordDialogFragment.ARG_ACTION;
import static org.odk.collect.android.preferences.AdminPasswordDialogFragment.Action.ADMIN_SETTINGS;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class AdminPasswordDialogFragmentTest {

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public AdminPasswordProvider providesAdminPasswordProvider(PreferencesDataSourceProvider preferencesDataSourceProvider) {
                return new StubAdminPasswordProvider();
            }
        });
    }

    @Test
    public void enteringPassword_andClickingOK_callsOnCorrectAdminPasswordWithAction() {
        TestActivityScenario<SpyAdminPasswordDialogCallbackActivity> activityScenario = TestActivityScenario.launch(SpyAdminPasswordDialogCallbackActivity.class);
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = createFragment();
            fragment.show(activity.getSupportFragmentManager(), "tag");
            shadowOf(getMainLooper()).idle();

            fragment.getInput().setText("password");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            shadowOf(getMainLooper()).idle();

            assertThat(activity.onCorrectAdminPasswordCalledWith, equalTo(ADMIN_SETTINGS));
            assertThat(activity.onIncorrectAdminPasswordCalled, equalTo(false));
        });
    }

    @Test
    public void enteringIncorrectPassword_andClickingOK_callsOnInCorrectAdminPassword() {
        TestActivityScenario<SpyAdminPasswordDialogCallbackActivity> activityScenario = TestActivityScenario.launch(SpyAdminPasswordDialogCallbackActivity.class);
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = createFragment();
            fragment.show(activity.getSupportFragmentManager(), "tag");
            shadowOf(getMainLooper()).idle();

            fragment.getInput().setText("not the password");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            shadowOf(getMainLooper()).idle();

            assertThat(activity.onCorrectAdminPasswordCalledWith, nullValue());
            assertThat(activity.onIncorrectAdminPasswordCalled, equalTo(true));
        });
    }

    @Test
    public void afterRecreating_enteringPassword_andClickingOK_callsOnCorrectAdminPasswordWithAction() {
        TestActivityScenario<SpyAdminPasswordDialogCallbackActivity> activityScenario = TestActivityScenario.launch(SpyAdminPasswordDialogCallbackActivity.class);
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = createFragment();
            fragment.show(activity.getSupportFragmentManager(), "tag");
            shadowOf(getMainLooper()).idle();
        });

        activityScenario.recreate();
        activityScenario.onActivity(activity -> {
            AdminPasswordDialogFragment fragment = (AdminPasswordDialogFragment) activity.getSupportFragmentManager().findFragmentByTag("tag");

            fragment.getInput().setText("password");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            shadowOf(getMainLooper()).idle();

            assertThat(activity.onCorrectAdminPasswordCalledWith, equalTo(ADMIN_SETTINGS));
            assertThat(activity.onIncorrectAdminPasswordCalled, equalTo(false));
        });
    }

    private AdminPasswordDialogFragment createFragment() {
        AdminPasswordDialogFragment fragment = new AdminPasswordDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ACTION, ADMIN_SETTINGS);
        fragment.setArguments(args);
        return fragment;
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

    private static class SpyAdminPasswordDialogCallbackActivity extends FragmentActivity implements AdminPasswordDialogFragment.AdminPasswordDialogCallback  {

        private AdminPasswordDialogFragment.Action onCorrectAdminPasswordCalledWith;
        private boolean onIncorrectAdminPasswordCalled;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(R.style.Theme_AppCompat); // Needed for androidx.appcompat.app.AlertDialog
        }

        @Override
        public void onCorrectAdminPassword(AdminPasswordDialogFragment.Action action) {
            onCorrectAdminPasswordCalledWith = action;
        }

        @Override
        public void onIncorrectAdminPassword() {
            onIncorrectAdminPasswordCalled = true;
        }
    }
}