
package org.odk.collect.android.utilities;

import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.AuditConfig;
import org.odk.collect.android.logic.AuditEvent;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.AuditEventSaveTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.Nullable;
import timber.log.Timber;

import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_PROVIDERS_DISABLED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventType.LOCATION_PROVIDERS_ENABLED;
import static org.odk.collect.android.logic.FormController.AUDIT_FILE_NAME;

/**
 * Handle logging of auditEvents (which contain time and might contain location coordinates),
 * and pass them to an Async task to append to a file
 * Notes:
 * 1) If the user has saved the form, then resumes editing, then exits without saving then the timing data during the
 * second editing session will be saved.  This is OK as it records user activity.  However if the user exits
 * without saving and they have never saved the form then the timing data is lost as the form editing will be
 * restarted from scratch.
 * 2) The auditEvents for questions in a field-list group are not shown.  Only the event for the group is shown.
 */
public class AuditEventLogger {

    private List<Location> locations = new ArrayList<>();

    private static AsyncTask saveTask;
    private ArrayList<AuditEvent> auditEvents = new ArrayList<>();
    private File auditFile;
    private long surveyOpenTime;
    private long surveyOpenElapsedTime;
    private final AuditConfig auditConfig;

    public AuditEventLogger(File instanceFile, AuditConfig auditConfig) {
        this.auditConfig = auditConfig;

        if (isAuditEnabled() && instanceFile != null) {
            auditFile = new File(instanceFile.getParentFile().getPath() + File.separator + AUDIT_FILE_NAME);
        }
    }

    public void logEvent(AuditEvent.AuditEventType eventType, boolean writeImmediatelyToDisk) {
        logEvent(eventType, null, writeImmediatelyToDisk, null);
    }

    /*
     * Log a new event
     */
    public void logEvent(AuditEvent.AuditEventType eventType, FormIndex formIndex,
                         boolean writeImmediatelyToDisk, String questionAnswer) {
        if (!isAuditEnabled() || shouldBeIgnored(eventType)) {
            return;
        }

        Timber.i("AuditEvent recorded: %s", eventType);

        AuditEvent newAuditEvent = new AuditEvent(getEventTime(), eventType, auditConfig.isLocationEnabled(),
                auditConfig.isTrackingChangesEnabled(), formIndex, questionAnswer);

        if (isDuplicatedIntervalEvent(newAuditEvent)) {
            return;
        }

        if (auditConfig.isLocationEnabled()) {
            addLocationCoordinatesToAuditEvent(newAuditEvent);
        }

        /*
         * Close any existing interval events if the view is being exited
         */
        if (eventType == AuditEvent.AuditEventType.FORM_EXIT) {
            manageSavedEvents();
        }

        auditEvents.add(newAuditEvent);

        /*
         * Write the event unless it is an interval event in which case we need to wait for the end of that event
         */
        if (writeImmediatelyToDisk && !newAuditEvent.isIntervalAuditEventType()) {
            writeEvents();
        }
    }

    private void addLocationCoordinatesToAuditEvent(AuditEvent auditEvent) {
        Location location = getMostAccurateLocation();
        String latitude = location != null ? Double.toString(location.getLatitude()) : "";
        String longitude = location != null ? Double.toString(location.getLongitude()) : "";
        String accuracy = location != null ? Double.toString(location.getAccuracy()) : "";
        auditEvent.setLocationCoordinates(latitude, longitude, accuracy);
    }

    private void addNewValueToQuestionAuditEvent(AuditEvent aev, FormController formController) {
        IAnswerData answerData = formController.getQuestionPrompt(aev.getFormIndex()).getAnswerValue();
        aev.recordValueChange(answerData != null ? answerData.getDisplayText() : null);
    }

    // If location provider are enabled/disabled it sometimes fires the BroadcastReceiver multiple
    // times what tries to add duplicated logs
    boolean isDuplicateOfLastLocationEvent(AuditEvent.AuditEventType eventType) {
        return (eventType.equals(LOCATION_PROVIDERS_ENABLED) || eventType.equals(LOCATION_PROVIDERS_DISABLED))
                && !auditEvents.isEmpty() && eventType.equals(auditEvents.get(auditEvents.size() - 1).getAuditEventType());
    }

    /*
     * Ignore the event if we are already in an interval view event or have jumped
     * This can happen if the user is on a question page and the page gets refreshed
     * The exception is hierarchy events since they interrupt an existing interval event
     */
    private boolean isDuplicatedIntervalEvent(AuditEvent newAuditEvent) {
        if (newAuditEvent.isIntervalAuditEventType()) {
            for (AuditEvent aev : auditEvents) {
                if (aev.isIntervalAuditEventType()
                        && newAuditEvent.getAuditEventType().equals(aev.getAuditEventType())
                        && newAuditEvent.getFormIndex().equals(aev.getFormIndex())) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Exit a question
     */
    public void exitView() {
        if (isAuditEnabled()) {
            manageSavedEvents();
            writeEvents();
        }
    }

    // Filter all events and set final parameters of interval events
    private void manageSavedEvents() {
        // Calculate the time and add the event to the auditEvents array
        long end = getEventTime();
        ArrayList<AuditEvent> filteredAuditEvents = new ArrayList<>();
        for (AuditEvent aev : auditEvents) {
            if (aev.isIntervalAuditEventType()) {
                setIntervalEventFinalParameters(aev, end);
            }
            if (shouldEventBeLogged(aev)) {
                filteredAuditEvents.add(aev);
            }
        }

        auditEvents.clear();
        auditEvents.addAll(filteredAuditEvents);
    }

    private void setIntervalEventFinalParameters(AuditEvent aev, long end) {
        // Set location parameters.
        // We try to add them here again (first attempt takes place when an event is being created),
        // because coordinates might be not available at that time, so now we have another (last) chance.
        if (auditConfig.isLocationEnabled() && !aev.isLocationAlreadySet()) {
            addLocationCoordinatesToAuditEvent(aev);
        }

        // Set answers
        FormController formController = Collect.getInstance().getFormController();
        if (aev.getAuditEventType().equals(AuditEvent.AuditEventType.QUESTION) && formController != null) {
            addNewValueToQuestionAuditEvent(aev, formController);
        }

        // Set end time
        if (!aev.isEndTimeSet()) {
            aev.setEnd(end);
        }
    }

    /**
     * @return true if an event of this type should be ignored given the current audit configuration
     * and previous events, false otherwise.
     */
    private boolean shouldBeIgnored(AuditEvent.AuditEventType eventType) {
        return !eventType.isLogged()
                || eventType.isLocationRelated() && !auditConfig.isLocationEnabled()
                || isDuplicateOfLastLocationEvent(eventType);
    }

    /*
    Question which is in field-list group should be logged only if tracking changes option is
    enabled and its answer has changed
    */
    private boolean shouldEventBeLogged(AuditEvent aev) {
        FormController formController = Collect.getInstance().getFormController();
        if (aev.getAuditEventType().equals(AuditEvent.AuditEventType.QUESTION) && formController != null) {
            return !formController.indexIsInFieldList(aev.getFormIndex())
                    || (aev.hasNewAnswer() && auditConfig.isTrackingChangesEnabled());
        }
        return true;
    }

    private void writeEvents() {
        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {
            AuditEvent[] auditEventArray = auditEvents.toArray(new AuditEvent[0]);
            if (auditFile != null) {
                saveTask = new AuditEventSaveTask(auditFile, auditConfig.isLocationEnabled(), auditConfig.isTrackingChangesEnabled()).execute(auditEventArray);
            } else {
                Timber.e("auditFile null when attempting to write auditEvents.");
            }
            auditEvents = new ArrayList<>();
        } else {
            Timber.i("Queueing AuditEvent");
        }
    }

    /*
     * Use the time the survey was opened as a consistent value for wall clock time
     */
    private long getEventTime() {
        if (surveyOpenTime == 0) {
            surveyOpenTime = System.currentTimeMillis();
            surveyOpenElapsedTime = SystemClock.elapsedRealtime();
        }
        return surveyOpenTime + (SystemClock.elapsedRealtime() - surveyOpenElapsedTime);
    }

    public void addLocation(Location location) {
        locations.add(location);
    }

    @Nullable
    Location getMostAccurateLocation() {
        removeExpiredLocations();

        Location bestLocation = null;
        if (!locations.isEmpty()) {
            for (Location location : locations) {
                if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = location;
                }
            }
        }
        return bestLocation;
    }

    private void removeExpiredLocations() {
        if (!locations.isEmpty()) {
            List<Location> unexpiredLocations = new ArrayList<>();
            for (Location location : locations) {
                if (System.currentTimeMillis() <= location.getTime() + auditConfig.getLocationMaxAge()) {
                    unexpiredLocations.add(location);
                }
            }
            locations = unexpiredLocations;
        }
    }

    /*
     * The event logger is enabled if the meta section of the form contains a logging entry
     *      <orx:audit />
     */
    boolean isAuditEnabled() {
        return auditConfig != null;
    }

    ArrayList<AuditEvent> getAuditEvents() {
        return auditEvents;
    }

    List<Location> getLocations() {
        return locations;
    }
}