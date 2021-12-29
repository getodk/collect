package org.odk.collect.android.preferences.screens;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.support.CollectHelpers;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.permissions.PermissionListener;
import org.odk.collect.permissions.PermissionsChecker;
import org.odk.collect.permissions.PermissionsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_METADATA_PHONENUMBER;

@RunWith(AndroidJUnit4.class)
public class FormMetadataPreferencesFragmentTest {

    private final FakePhoneStatePermissionsProvider permissionsProvider = new FakePhoneStatePermissionsProvider();
    private final DeviceDetailsProvider deviceDetailsProvider = mock(DeviceDetailsProvider.class);

    @Before
    public void setup() {
        CollectHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
                return permissionsProvider;
            }

            @Override
            public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
                return deviceDetailsProvider;
            }
        });
    }

    @Test
    public void recreating_doesntRequestPermissionsAgain() {
        FragmentScenario<FormMetadataPreferencesFragment> scenario = FragmentScenario.launch(FormMetadataPreferencesFragment.class);
        assertThat(permissionsProvider.timesRequested, equalTo(1));

        scenario.recreate();
        assertThat(permissionsProvider.timesRequested, equalTo(1));
    }

    @Test
    public void recreating_whenPermissionsAcceptedPreviously_showsPermissionDependantPreferences() {
        when(deviceDetailsProvider.getDeviceId()).thenReturn("123456789");

        FragmentScenario<FormMetadataPreferencesFragment> scenario = FragmentScenario.launch(FormMetadataPreferencesFragment.class);
        permissionsProvider.grant();
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo("123456789"));
        });

        scenario.recreate();
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo("123456789"));
        });
    }

    @Test
    public void recreating_whenPermissionsGrantedPreviously_doesNotShowPermissionDependantPreferences() {
        FragmentScenario<FormMetadataPreferencesFragment> scenario = FragmentScenario.launch(FormMetadataPreferencesFragment.class);
        permissionsProvider.deny();
        scenario.recreate();
        verifyNoInteractions(deviceDetailsProvider);
    }

    @Test
    public void whenDeviceDetailsAreMissing_preferenceSummariesAreNotSet() {
        when(deviceDetailsProvider.getLine1Number()).thenReturn(null);
        when(deviceDetailsProvider.getDeviceId()).thenReturn(null);

        FragmentScenario<FormMetadataPreferencesFragment> scenario = FragmentScenario.launch(FormMetadataPreferencesFragment.class);
        permissionsProvider.grant();
        scenario.onFragment(fragment -> {
            String notSetMessage = fragment.getContext().getString(R.string.preference_not_available);

            assertThat(fragment.findPreference(KEY_METADATA_PHONENUMBER).getSummary(), equalTo(notSetMessage));
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo(notSetMessage));
        });
    }

    private static class FakePhoneStatePermissionsProvider extends PermissionsProvider {

        int timesRequested;
        private PermissionListener lastAction;
        private boolean granted;

        private FakePhoneStatePermissionsProvider() {
            super(new PermissionsChecker(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        }

        @Override
        public void requestReadPhoneStatePermission(Activity activity, boolean displayPermissionDeniedDialog, @NonNull PermissionListener action) {
            timesRequested++;
            this.lastAction = action;
        }

        @Override
        public boolean isReadPhoneStatePermissionGranted() {
            return granted;
        }

        void grant() {
            granted = true;
            lastAction.granted();
        }

        void deny() {
            granted = false;
            lastAction.denied();
        }
    }
}
