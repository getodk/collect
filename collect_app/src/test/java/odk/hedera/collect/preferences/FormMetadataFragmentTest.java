package odk.hedera.collect.preferences;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.hedera.collect.R;
import odk.hedera.collect.injection.config.AppDependencyModule;
import odk.hedera.collect.listeners.PermissionListener;
import org.odk.hedera.collect.support.RobolectricHelpers;
import odk.hedera.collect.utilities.DeviceDetailsProvider;
import odk.hedera.collect.utilities.PermissionUtils;
import org.robolectric.annotation.LooperMode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static odk.hedera.collect.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static odk.hedera.collect.logic.PropertyManager.PROPMGR_SIM_SERIAL;
import static odk.hedera.collect.logic.PropertyManager.PROPMGR_SUBSCRIBER_ID;
import static odk.hedera.collect.preferences.GeneralKeys.KEY_METADATA_PHONENUMBER;
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
            public DeviceDetailsProvider providesDeviceDetailsProvider(Context context) {
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
        when(deviceDetailsProvider.getSimSerialNumber()).thenReturn("simSerial");

        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionUtils.grant();
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(PROPMGR_SIM_SERIAL).getSummary(), equalTo("simSerial"));
        });

        scenario.recreate();
        scenario.onFragment(fragment -> {
            assertThat(fragment.findPreference(PROPMGR_SIM_SERIAL).getSummary(), equalTo("simSerial"));
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
        when(deviceDetailsProvider.getSimSerialNumber()).thenReturn(null);
        when(deviceDetailsProvider.getLine1Number()).thenReturn(null);
        when(deviceDetailsProvider.getSubscriberId()).thenReturn(null);
        when(deviceDetailsProvider.getDeviceId()).thenReturn(null);

        FragmentScenario<FormMetadataFragment> scenario = FragmentScenario.launch(FormMetadataFragment.class);
        permissionUtils.grant();
        scenario.onFragment(fragment -> {
            String notSetMessage = fragment.getContext().getString(R.string.preference_not_available);

            assertThat(fragment.findPreference(PROPMGR_SIM_SERIAL).getSummary(), equalTo(notSetMessage));
            assertThat(fragment.findPreference(KEY_METADATA_PHONENUMBER).getSummary(), equalTo(notSetMessage));
            assertThat(fragment.findPreference(PROPMGR_SUBSCRIBER_ID).getSummary(), equalTo(notSetMessage));
            assertThat(fragment.findPreference(PROPMGR_DEVICE_ID).getSummary(), equalTo(notSetMessage));
        });
    }

    private static class FakePhoneStatePermissionUtils extends PermissionUtils {

        int timesRequested;
        private PermissionListener lastAction;
        private boolean granted;

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