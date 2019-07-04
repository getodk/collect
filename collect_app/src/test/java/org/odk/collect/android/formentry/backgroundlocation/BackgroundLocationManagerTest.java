package org.odk.collect.android.formentry.backgroundlocation;

import android.location.Location;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.location.LocationTestUtils;
import org.odk.collect.android.location.client.FakeLocationClient;
import org.odk.collect.android.logic.AuditConfig;
import org.odk.collect.android.logic.AuditEvent;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BackgroundLocationManagerTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private BackgroundLocationManager backgroundLocationManager;

    private FakeLocationClient fakeLocationClient;
    @Mock
    private BackgroundLocationHelper locationHelper;

    @Before
    public void setUp() {
        fakeLocationClient = new FakeLocationClient();
        backgroundLocationManager = new BackgroundLocationManager(fakeLocationClient, locationHelper);
    }

    /**
     * activityDisplayed is called from both onStart and loadingComplete in FormEntryActivity. This
     * test confirms that it doesn't matter how many times activityDisplayed is called until the
     * form has been loaded.
     */
    @Test
    public void displayingActivityManyTimes_ShouldHaveNoEffect_WhenFormNotLoaded() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);

        assertThat(backgroundLocationManager.activityDisplayed(), is(nullValue()));
        assertThat(backgroundLocationManager.isPendingPermissionCheck(), is(false));

        assertThat(backgroundLocationManager.activityDisplayed(), is(nullValue()));
        assertThat(backgroundLocationManager.isPendingPermissionCheck(), is(false));

        verify(locationHelper, never()).logAuditEvent(Mockito.any());

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();

        assertThat(backgroundLocationManager.isPendingPermissionCheck(), is(true));
        verify(locationHelper, atLeastOnce()).logAuditEvent(Mockito.any());
    }

    @Test
    public void locationPermissions_ShouldBeRequested_WhenFormRequestsBackgroundLocation_AndPreconditionsAreMet() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);

        backgroundLocationManager.formFinishedLoading();

        assertThat(backgroundLocationManager.activityDisplayed(), is(nullValue()));
        verify(locationHelper, never()).logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
        verify(locationHelper, never()).logAuditEvent(AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);

        assertThat(backgroundLocationManager.isPendingPermissionCheck(), is(true));
    }

    @Test
    public void locationPermissionDenied_ShouldBeLogged_WhenFormAuditsLocation() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();

        assertThat(backgroundLocationManager.isPendingPermissionCheck(), is(true));

        backgroundLocationManager.locationPermissionDenied();

        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.LOCATION_PERMISSIONS_NOT_GRANTED);
    }

    @Test
    public void playServicesWarning_ShouldBeReturnedAndLogged_WhenFormRequestsBackgroundLocation_AndPlayServicesNotAvailable() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(false);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);

        backgroundLocationManager.formFinishedLoading();
        assertThat(backgroundLocationManager.activityDisplayed(), is(BackgroundLocationManager.BackgroundLocationMessage.PLAY_SERVICES_UNAVAILABLE));

        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
        verify(locationHelper, never()).logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
    }

    @Test
    public void preferencesWarning_ShouldBeReturnedAndLogged_WhenFormRequestsBackgroundLocation_AndPreferenceDisabled() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(false);

        backgroundLocationManager.formFinishedLoading();
        assertThat(backgroundLocationManager.activityDisplayed(), is(BackgroundLocationManager.BackgroundLocationMessage.LOCATION_PREF_DISABLED));

        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
        verify(locationHelper, never()).logAuditEvent(AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
    }

    @Test
    public void playServicesWarning_ShouldBeReturned_WhenFormRequestsBackgroundLocation_AndNoPreconditionMet() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(false);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(false);

        backgroundLocationManager.formFinishedLoading();
        assertThat(backgroundLocationManager.activityDisplayed(), is(BackgroundLocationManager.BackgroundLocationMessage.PLAY_SERVICES_UNAVAILABLE));

        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
    }

    @Test
    public void providersWarning_ShouldBeReturned_WhenFormRequestsLocationAudit_AndNoProviderAvailable() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo",  "2", "3", true));

        fakeLocationClient.setLocationAvailable(false);

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();
        assertThat(backgroundLocationManager.locationPermissionGranted(), is(BackgroundLocationManager.BackgroundLocationMessage.PROVIDERS_DISABLED));
    }

    @Test
    public void locationTrackingWarning_ShouldBeReturned_WhenFormRequestsBackgroundLocation_AndAllPreconditionsMet() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();

        assertThat(backgroundLocationManager.locationPermissionGranted(), is(BackgroundLocationManager.BackgroundLocationMessage.COLLECTING_LOCATION));
    }

    @Test
    public void locationChanges_ShouldBeSentToAuditLogger_WhenFormAuditsLocation_AndAllPreconditionsMet() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo", "2", "3", true));

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();
        backgroundLocationManager.locationPermissionGranted();

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);

        verify(locationHelper).provideLocationToAuditLogger(location);
    }

    @Test
    public void locationChanges_ShouldNotBeSentToAuditLogger_WhenFormHasSetlocationAndNoAudit() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(false);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();
        backgroundLocationManager.locationPermissionGranted();

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);

        verify(locationHelper, never()).provideLocationToAuditLogger(location);
    }

    @Test
    public void revokingLocationPermission_ShouldResultInLocationUpdatesStopping_WhenFormAuditsLocation() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo", "2", "3", true));

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();
        backgroundLocationManager.locationPermissionGranted();

        // User revokes permission in Android settings
        backgroundLocationManager.activityHidden();
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(false);
        // No snackbar is shown on activity re-entry
        assertThat(backgroundLocationManager.activityDisplayed(), is(nullValue()));

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);

        verify(locationHelper, never()).provideLocationToAuditLogger(location);
    }

    @Test
    public void grantingLocationPermission_ShouldResultInLocationUpdatesStarting_WhenFormAuditsLocation() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(false);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo", "2", "3", true));

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();
        backgroundLocationManager.locationPermissionDenied();

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);
        verify(locationHelper, never()).provideLocationToAuditLogger(location);

        // User grants permission in Android settings
        backgroundLocationManager.activityHidden();
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        // No snackbar is shown on activity re-entry
        assertThat(backgroundLocationManager.activityDisplayed(), is(nullValue()));

        fakeLocationClient.receiveFix(location);
        verify(locationHelper).provideLocationToAuditLogger(location);
    }

    @Test
    public void togglingBackgroundLocationPreference_ShouldToggleLocationCapture() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo", "2", "3", true));

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();
        backgroundLocationManager.locationPermissionGranted();

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);

        verify(locationHelper).provideLocationToAuditLogger(location);

        // Toggle preference off
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(false);
        backgroundLocationManager.backgroundLocationPreferenceToggled();

        Location location2 = LocationTestUtils.createLocation("GPS", 7, 2, 3, 4);
        fakeLocationClient.receiveFix(location2);

        verify(locationHelper, never()).provideLocationToAuditLogger(location2);

        // Toggle preference on
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        backgroundLocationManager.backgroundLocationPreferenceToggled();

        fakeLocationClient.receiveFix(location2);
        verify(locationHelper).provideLocationToAuditLogger(location2);
    }

    @Test
    public void changesInLocationProviders_ShouldBeAudited_IfFormRequestsAudit_AndAllPreconditionsMet() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo", "2", "3", true));

        backgroundLocationManager.formFinishedLoading();

        backgroundLocationManager.activityDisplayed();
        verify(locationHelper, never()).logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED);
        verify(locationHelper, never()).logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED);

        backgroundLocationManager.locationPermissionGranted();
        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED);

        // Providers off
        fakeLocationClient.setLocationAvailable(false);
        backgroundLocationManager.locationProvidersChanged();
        verify(locationHelper).logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED);

        // Providers back on
        fakeLocationClient.setLocationAvailable(true);
        backgroundLocationManager.locationProvidersChanged();
        verify(locationHelper, times(2)).logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED);
    }

    @Test
    public void locationEvents_ShouldNeverBeLogged_WhenFormDoesNotRequestLocation() {
        when(locationHelper.currentFormCollectsBackgroundLocation()).thenReturn(false);

        backgroundLocationManager.formFinishedLoading();
        backgroundLocationManager.activityDisplayed();

        backgroundLocationManager.activityHidden();
        backgroundLocationManager.activityDisplayed();

        // not possible through FormEntryActivity because menu doesn't show toggle
        backgroundLocationManager.backgroundLocationPreferenceToggled();

        // not possible through FormEntryActivity because provider not registered
        backgroundLocationManager.locationProvidersChanged();

        // not possible through FormEntryActivity because listener not set
        backgroundLocationManager.onLocationChanged(LocationTestUtils.createLocation("GPS", 1, 2, 3, 4));

        // not possible through FormEntryActivity because location request never needed
        backgroundLocationManager.locationPermissionGranted();
        backgroundLocationManager.locationPermissionDenied();

        verify(locationHelper, never()).logAuditEvent(Mockito.any());
    }

    /**
     * Simulates the case where the system kills the activity. The form doesn't need to be loaded
     * again because FormController is an Application-scoped singleton so if the form audits location,
     * we should go straight to requesting location without showing anything to the user.
     **/
    @Test
    public void locationRequests_ShouldResume_WhenActivityIsDisplayed_AndFormThatAuditsLocationWasAlreadyLoaded() {
        when(locationHelper.isCurrentFormSet()).thenReturn(true);
        when(locationHelper.currentFormAuditsLocation()).thenReturn(true);

        when(locationHelper.arePlayServicesAvailable()).thenReturn(true);
        when(locationHelper.isBackgroundLocationPreferenceEnabled()).thenReturn(true);
        when(locationHelper.isAndroidLocationPermissionGranted()).thenReturn(true);
        when(locationHelper.getCurrentFormAuditConfig()).thenReturn(new AuditConfig("foo", "2", "3", true));

        backgroundLocationManager.activityDisplayed();

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);
        verify(locationHelper).provideLocationToAuditLogger(location);
    }

    @Test
    public void locationRequests_ShouldNotResume_WhenActivityIsDisplayed_AndFormThatDoesNotAuditLocationWasAlreadyLoaded() {
        when(locationHelper.isCurrentFormSet()).thenReturn(true);
        backgroundLocationManager.activityDisplayed();

        Location location = LocationTestUtils.createLocation("GPS", 1, 2, 3, 4);
        fakeLocationClient.receiveFix(location);
        verify(locationHelper, never()).provideLocationToAuditLogger(location);
    }
}
