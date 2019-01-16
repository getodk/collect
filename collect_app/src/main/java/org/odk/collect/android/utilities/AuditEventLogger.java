
package org.odk.collect.android.utilities;

import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;

import org.javarosa.core.model.instance.TreeReference;
import org.odk.collect.android.logic.AuditConfig;
import org.odk.collect.android.logic.AuditEvent;
import org.odk.collect.android.tasks.AuditEventSaveTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.Nullable;
import timber.log.Timber;

import static org.odk.collect.android.logic.AuditEvent.AuditEventTypes.LOCATION_PROVIDERS_DISABLED;
import static org.odk.collect.android.logic.AuditEvent.AuditEventTypes.LOCATION_PROVIDERS_ENABLED;
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

    /*
     * Log a new event
     */
    public void logEvent(AuditEvent.AuditEventTypes eventType, TreeReference ref, boolean writeImmediatelyToDisk) {
        if (isAuditEnabled() && !isDuplicateOfLastAuditEvent(eventType)) {
            Timber.i("AuditEvent recorded: %s", eventType);
            // Calculate the time and add the event to the auditEvents array
            long start = getEventTime();

            // Set the node value from the question reference
            String node = ref == null ? "" : ref.toString();
            if (eventType == AuditEvent.AuditEventTypes.QUESTION || eventType == AuditEvent.AuditEventTypes.GROUP) {
                int idx = node.lastIndexOf('[');
                if (idx > 0) {
                    node = node.substring(0, idx);
                }
            }

            AuditEvent newAuditEvent = new AuditEvent(start, eventType, node);
            addLocationCoordinatesToAuditEventIfNeeded(newAuditEvent);

            /*
             * Close any existing interval events if the view is being exited
             */
            if (newAuditEvent.auditEventType == AuditEvent.AuditEventTypes.FORM_EXIT) {
                for (AuditEvent aev : auditEvents) {
                    if (!aev.endTimeSet && aev.isIntervalViewEvent()) {
                        aev.setEnd(start);
                    }
                }
            }

            /*
             * Ignore the event if we are already in an interval view event or have jumped
             * This can happen if the user is on a question page and the page gets refreshed
             * The exception is hierarchy events since they interrupt an existing interval event
             */
            if (newAuditEvent.isIntervalViewEvent()) {
                for (AuditEvent aev : auditEvents) {
                    if (aev.isIntervalViewEvent() && !aev.endTimeSet) {
                        return;
                    }
                }
            }

            /*
             * Ignore beginning of form events and repeat events
             */
            if (newAuditEvent.auditEventType == AuditEvent.AuditEventTypes.BEGINNING_OF_FORM || newAuditEvent.auditEventType == AuditEvent.AuditEventTypes.REPEAT) {
                return;
            }

            /*
             * Having got to this point we are going to keep the event
             */
            auditEvents.add(newAuditEvent);

            /*
             * Write the event unless it is an interval event in which case we need to wait for the end of that event
             */
            if (writeImmediatelyToDisk && !newAuditEvent.isIntervalViewEvent()) {
                writeEvents();
            }
        }
    }

    private void addLocationCoordinatesToAuditEventIfNeeded(AuditEvent auditEvent) {
        if (auditConfig.isLocationEnabled()) {
            Location location = getMostAccurateLocation();
            String latitude = location != null ? Double.toString(location.getLatitude()) : "";
            String longitude = location != null ? Double.toString(location.getLongitude()) : "";
            String accuracy = location != null ? Double.toString(location.getAccuracy()) : "";
            if (!auditEvent.hasLocation()) {
                auditEvent.setLocationCoordinates(latitude, longitude, accuracy);
            }
        }
    }

    // If location provider are enabled/disabled it sometimes fires the BroadcastReceiver multiple
    // times what tries to add duplicated logs
    private boolean isDuplicateOfLastAuditEvent(AuditEvent.AuditEventTypes eventType) {
        return (eventType.equals(LOCATION_PROVIDERS_ENABLED) || eventType.equals(LOCATION_PROVIDERS_DISABLED))
                && !auditEvents.isEmpty() && eventType.equals(auditEvents.get(auditEvents.size() - 1).auditEventType);
    }

    /*
     * Exit a question
     */
    public void exitView() {
        if (isAuditEnabled()) {
            // Calculate the time and add the event to the auditEvents array
            long end = getEventTime();
            for (AuditEvent aev : auditEvents) {
                if (!aev.endTimeSet && aev.isIntervalViewEvent()) {
                    addLocationCoordinatesToAuditEventIfNeeded(aev);
                    aev.setEnd(end);
                }
            }

            writeEvents();
        }
    }

    private void writeEvents() {
        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {
            AuditEvent[] auditEventArray = auditEvents.toArray(new AuditEvent[auditEvents.size()]);
            if (auditFile != null) {
                saveTask = new AuditEventSaveTask(auditFile, auditConfig.isLocationEnabled()).execute(auditEventArray);
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
    private Location getMostAccurateLocation() {
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
     *      <orx:auditConfig />
     */
    private boolean isAuditEnabled() {
        return auditConfig != null;
    }
}