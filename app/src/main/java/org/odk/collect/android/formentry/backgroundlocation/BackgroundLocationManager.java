package org.odk.collect.android.formentry.backgroundlocation;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationListener;

import org.odk.collect.android.R;
import org.odk.collect.location.LocationClient;
import org.odk.collect.android.formentry.audit.AuditConfig;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction;

/**
 * Manages background location for the location audit logging and odk:setgeopoint action features.
 * Provides precondition checking and user feedback for both features.
 *
 * For location audit logging, manages all the audit logging as well as fetching the location using
 * Google Play Services.
 *
 * {@link CollectSetGeopointAction} fetches location for odk:setgeopoint actions.
 *
 * The implementation uses a state machine concept. Public methods represent user or system actions
 * that clients of this class react to. Based on those actions and various preconditions (Google Play
 * Services available, location permissions granted, etc), the manager's state changes.
 */
public class BackgroundLocationManager implements LocationClient.LocationClientListener, LocationListener {
    @NonNull
    private BackgroundLocationState currentState;

    @NonNull
    private LocationClient locationClient;

    @NonNull
    private LocationListener locationListener;

    @NonNull
    private BackgroundLocationHelper helper;

    public BackgroundLocationManager(LocationClient locationClient, BackgroundLocationHelper helper) {
        currentState = BackgroundLocationState.NO_BACKGROUND_LOCATION_NEEDED;
        this.locationClient = locationClient;

        this.helper = helper;

        this.locationListener = this;
    }

    public void formFinishedLoading() {
        switch (currentState) {
            case NO_BACKGROUND_LOCATION_NEEDED:
                if (helper.currentFormCollectsBackgroundLocation()) {
                    currentState = BackgroundLocationState.PENDING_PRECONDITION_CHECKS;
                }
        }
    }

    public BackgroundLocationMessage activityDisplayed() {
        switch (currentState) {
            case NO_BACKGROUND_LOCATION_NEEDED:
                // After system-initiated process death, state is reset. The form did not get
                // reloaded and user messaging has already been displayed so go straight to
                // requesting location.
                if (helper.isCurrentFormSet() && helper.currentFormAuditsLocation()) {
                    startLocationRequests();

                    if (currentState != BackgroundLocationState.RECEIVING_LOCATIONS) {
                        // The form requests background location and some precondition failed. Change
                        // the state to STOPPED so that if preconditions change, location tracking
                        // will resume.
                        currentState = BackgroundLocationState.STOPPED;
                    }
                }
                break;

            case PENDING_PRECONDITION_CHECKS:
                // Separate out user message so that any failed precondition is written to the audit
                // log. If Play Services are unavailable AND the location preference is disabled,
                // show the Play Services unavailable message only.
                BackgroundLocationMessage userMessage = null;

                if (!helper.isBackgroundLocationPreferenceEnabled()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
                    userMessage = BackgroundLocationMessage.LOCATION_PREF_DISABLED;
                }

                if (!helper.arePlayServicesAvailable()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE);
                    userMessage = BackgroundLocationMessage.PLAY_SERVICES_UNAVAILABLE;
                }

                if (userMessage == null) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_ENABLED);
                    currentState = BackgroundLocationState.PENDING_PERMISSION_CHECK;
                } else {
                    currentState = BackgroundLocationState.STOPPED;
                }

                return userMessage;

            case STOPPED:
                // All preconditions could be met either because we were collecting location, hid
                // the activity and showed it again or because a precondition became met.
                startLocationRequests();
                break;
        }

        return null;
    }

    public void activityHidden() {
        switch (currentState) {
            case RECEIVING_LOCATIONS:
            case STOPPED:
                stopLocationRequests();
        }
    }

    public boolean isPendingPermissionCheck() {
        return currentState == BackgroundLocationState.PENDING_PERMISSION_CHECK;
    }

    public BackgroundLocationMessage locationPermissionGranted() {
        switch (currentState) {
            case PENDING_PERMISSION_CHECK:
                if (!helper.currentFormAuditsLocation()) {
                    // Since setgeopoint actions manage their own location clients, we don't need to configure
                    // location requests here before asking isLocationAvailable()
                    currentState = BackgroundLocationState.SETGEOPOINT_ONLY;
                    if (locationClient.isLocationAvailable()) {
                        return BackgroundLocationMessage.COLLECTING_LOCATION;
                    } else {
                        return BackgroundLocationMessage.PROVIDERS_DISABLED;
                    }
                }

                startLocationRequests();

                if (currentState != BackgroundLocationState.RECEIVING_LOCATIONS) {
                    // one of the preconditions became false; we don't want to stay PENDING_PERMISSION_CHECK
                    currentState = BackgroundLocationState.STOPPED;
                }

                helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PERMISSIONS_GRANTED);

                // TODO: isLocationAvailable must be called after location request made but there's no
                // guarantee since request updates are called onClientStart()
                if (locationClient.isLocationAvailable()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED);
                } else {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED);
                    return BackgroundLocationMessage.PROVIDERS_DISABLED;
                }

                return BackgroundLocationMessage.COLLECTING_LOCATION;
            default:
                return null;
        }
    }

    public void locationPermissionDenied() {
        switch (currentState) {
            case PENDING_PERMISSION_CHECK:
                if (!helper.currentFormAuditsLocation()) {
                    currentState = BackgroundLocationState.SETGEOPOINT_ONLY;
                    return;
                }

                helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PERMISSIONS_NOT_GRANTED);
                currentState = BackgroundLocationState.STOPPED;
        }
    }

    public void backgroundLocationPreferenceToggled() {
        switch (currentState) {
            case RECEIVING_LOCATIONS:
                if (!helper.isBackgroundLocationPreferenceEnabled()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_DISABLED);
                    stopLocationRequests();
                }
                break;

            case STOPPED:
                if (helper.isBackgroundLocationPreferenceEnabled()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_TRACKING_ENABLED);
                    startLocationRequests();
                }
                break;
        }
    }

    public void locationPermissionChanged() {
        switch (currentState) {
            case STOPPED:
                if (helper.isAndroidLocationPermissionGranted()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PERMISSIONS_GRANTED);
                } else {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PERMISSIONS_NOT_GRANTED);
                }
                break;
        }
    }

    public void locationProvidersChanged() {
        switch (currentState) {
            case RECEIVING_LOCATIONS:
            case STOPPED:
                if (locationClient.isLocationAvailable()) {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED);
                } else {
                    helper.logAuditEvent(AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED);
                }
                break;
        }
    }

    private void startLocationRequests() {
        if (helper.currentFormAuditsLocation()
                && helper.isBackgroundLocationPreferenceEnabled()
                && helper.arePlayServicesAvailable()
                && helper.isAndroidLocationPermissionGranted()) {
            AuditConfig auditConfig = helper.getCurrentFormAuditConfig();

            locationClient.setListener(this);
            locationClient.setPriority(auditConfig.getLocationPriority());
            locationClient.setUpdateIntervals(auditConfig.getLocationMinInterval(), auditConfig.getLocationMinInterval());
            locationClient.start();

            currentState = BackgroundLocationState.RECEIVING_LOCATIONS;
        }
    }

    private void stopLocationRequests() {
        locationClient.setListener(null);
        locationClient.stop();

        currentState = BackgroundLocationState.STOPPED;
    }

    @Override
    public void onLocationChanged(Location location) {
        switch (currentState) {
            case RECEIVING_LOCATIONS:
                helper.provideLocationToAuditLogger(location);
        }
    }

    @Override
    public void onClientStart() {
        locationClient.requestLocationUpdates(locationListener);
    }

    @Override
    public void onClientStartFailure() {

    }

    @Override
    public void onClientStop() {

    }

    private enum BackgroundLocationState {
        /** The current form does not track background location (also the case if the current form
         * is not set yet */
        NO_BACKGROUND_LOCATION_NEEDED,

        /** The current form tracks background location and a message hasn't been shown to the user **/
        PENDING_PRECONDITION_CHECKS,

        /** An Android location permission check must be performed */
        PENDING_PERMISSION_CHECK,

        /** Terminal state: all checks have been performed and messaging has been displayed to the
         * user, it's now up to the setgeopoint action implementation to manage location fetching */
        SETGEOPOINT_ONLY,

        /** The current form requests location audits but some preconditions to location capture are
         * currently unmet. Once this state is reached, it's only possible to go between it and
         * {@link #RECEIVING_LOCATIONS} */
        STOPPED,

        /** The current form audits location and all preconditions to location capture have been
         * met. Once this state is reached, it's only possible to go between it and
         * {@link #STOPPED} */
        RECEIVING_LOCATIONS
    }

    public enum BackgroundLocationMessage {
        LOCATION_PREF_DISABLED(R.string.background_location_disabled, true),
        PLAY_SERVICES_UNAVAILABLE(R.string.google_play_services_not_available, false),
        COLLECTING_LOCATION(R.string.background_location_enabled, true),
        PROVIDERS_DISABLED(-1, false);

        private int messageTextResourceId;

        // Indicates whether the message text needs a "⋮" character inserted
        private boolean isMenuCharacterNeeded;

        BackgroundLocationMessage(int messageTextResourceId, boolean isMenuCharacterNeeded) {
            this.messageTextResourceId = messageTextResourceId;
            this.isMenuCharacterNeeded = isMenuCharacterNeeded;
        }

        public int getMessageTextResourceId() {
            return messageTextResourceId;
        }

        public boolean isMenuCharacterNeeded() {
            return isMenuCharacterNeeded;
        }
    }
}
