package org.odk.collect.android.preferences;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.utilities.DeviceDetailsProvider;
import org.odk.collect.android.utilities.PermissionUtils;
import org.robolectric.annotation.LooperMode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_PHONENUMBER;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class FormMetadataFragmentTest {

    private final FakePhoneStatePermissionUtils permissionUtils = new FakePhoneStatePermissionUtils();
    private final DeviceDetailsProvider deviceDetailsProvider = mock(DeviceDetailsProvider.class);

    @Before
    public void setup() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public PermissionUtils providesPermissionUtils() {
                return permissionUtils;
            }

            @Override
            public DeviceDetailsProvider providesDeviceDetailsProvider(Context context, InstallIDProvider installIDProvider) {
                return deviceDetailsProvider;
            }
        });
    }

    @Test
    public void recreating_doesntRequestPermissionsAgain() {
        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        assertThat(permissionUtils.timesRequested, equalTo(1));

        scenario.recreate();
        assertThat(permissionUtils.timesRequested, equalTo(1));
    }

    @Test
    public void recreating_whenPermissionsAcceptedPreviously_showsPermissionDependantPreferences() {
        when(deviceDetailsProvider.getDeviceId()).thenReturn("123456789");

        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionUtils.grant();
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
        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionUtils.deny();
        scenario.recreate();
        verifyNoInteractions(deviceDetailsProvider);
    }

    @Test
    public void whenDeviceDetailsAreMissing_preferenceSummariesAreNotSet() {
        when(deviceDetailsProvider.getLine1Number()).thenReturn(null);
        when(deviceDetailsProvider.getDeviceId()).thenReturn(null);

        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionUtils.grant();
        scenario.onFragment(fragment -> {
            String notSetMessage = fragment.getContext().getString(R.string.preference_not_available);

            assertThat(fragment.findPreference(KEY_METADATA_PHONENUMBER).getSummary(), equalTo(notSetMessage));
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo(notSetMessage));
        });
    }

    private static class FakePhoneStatePermissionUtils extends PermissionUtils {

        int timesRequested;
        private PermissionListener lastAction;
        private boolean granted;

        private FakePhoneStatePermissionUtils() {
            super(R.style.Theme_Collect_Dialog_PermissionAlert);
        }

        @Override
        public void requestReadPhoneStatePermission(Activity activity, boolean displayPermissionDeniedDialog, @NonNull PermissionListener action) {
            timesRequested++;
            this.lastAction = action;
        }

        @Override
        public boolean isReadPhoneStatePermissionGranted(Context context) {
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