
package org.odk.collect.android.utilities;

import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;

import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.logic.Audit;
import org.odk.collect.android.logic.Event;
import org.odk.collect.android.tasks.EventSaveTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.Nullable;
import timber.log.Timber;

import static org.odk.collect.android.logic.FormController.AUDIT_FILE_NAME;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.LOCATION_PROVIDERS_DISABLED;
import static org.odk.collect.android.utilities.EventLogger.EventTypes.LOCATION_PROVIDERS_ENABLED;

/**
 * Handle logging of events (which contain time and might contain location coordinates),
 * and pass them to an Async task to append to a file
 * Notes:
 * 1) If the user has saved the form, then resumes editing, then exits without saving then the timing data during the
 * second editing session will be saved.  This is OK as it records user activity.  However if the user exits
 * without saving and they have never saved the form then the timing data is lost as the form editing will be
 * restarted from scratch.
 * 2) The events for questions in a field-list group are not shown.  Only the event for the group is shown.
 */
public class EventLogger {

    private List<Location> locations = new ArrayList<>();

    public enum EventTypes {
        FEC,                // FEC, Real type defined in FormEntryController
        FORM_START,         // Start filling in the form
        FORM_EXIT,          // Exit the form
        FORM_RESUME,        // Resume filling in the form after previously exiting
        FORM_SAVE,          // Save the form
        FORM_FINALIZE,      // Finalize the form
        HIERARCHY,          // Jump to a question
        SAVE_ERROR,         // Error in save
        FINALIZE_ERROR,     // Error in finalize
        CONSTRAINT_ERROR,   // Constraint or missing answer error on save
        DELETE_REPEAT,      // Delete a repeat group
        GOOGLE_PLAY_SERVICES_NOT_AVAILABLE, // Google Play Services are not available
        LOCATION_PERMISSIONS_GRANTED, // Location permissions are granted
        LOCATION_PERMISSIONS_NOT_GRANTED, // Location permissions are not granted
        BACKGROUND_LOCATION_ENABLED, // Background location option is enabled
        BACKGROUND_LOCATION_DISABLED, // Background location option is disabled
        LOCATION_PROVIDERS_ENABLED, // Location providers are enabled
        LOCATION_PROVIDERS_DISABLED // Location providers are disabled
    }

    private static AsyncTask saveTask;
    private ArrayList<Event> events = new ArrayList<>();
    private File auditFile;
    private long surveyOpenTime;
    private long surveyOpenElapsedTime;
    private final boolean auditEnabled;              // Set true of the event logger is enabled
    private final Audit audit;

    public EventLogger(File instanceFile, Audit audit) {
        this.audit = audit;

        /*
         * The event logger is enabled if the meta section of the form contains a logging entry
         *      <orx:audit />
         */
        auditEnabled = audit != null;

        if (auditEnabled && instanceFile != null) {
            auditFile = new File(instanceFile.getParentFile().getPath() + File.separator + AUDIT_FILE_NAME);
        }
    }

    public void logEvent(EventTypes eventType,
                         TreeReference ref,
                         boolean writeImmediatelyToDisk) {
        logEvent(eventType, 0, ref, writeImmediatelyToDisk);
    }

    /*
     * Log a new event
     */
    public void logEvent(EventTypes eventType,
                         int fecType,
                         TreeReference ref,
                         boolean writeImmediatelyToDisk) {

        if (auditEnabled && !duplicatedLocationProvidersLog(eventType)) {

            Timber.i("Event recorded: %s : %s", eventType, fecType);
            // Calculate the time and add the event to the events array
            long start = getEventTime();

            // Set the node value from the question reference
            String node = ref == null ? "" : ref.toString();
            if (eventType == EventTypes.FEC
                    && (fecType == FormEntryController.EVENT_QUESTION
                    || fecType == FormEntryController.EVENT_GROUP)) {
                int idx = node.lastIndexOf('[');
                if (idx > 0) {
                    node = node.substring(0, idx);
                }
            }

            Event newEvent = new Event(start, eventType, fecType, node);
            addLocationCoordinatesToEventIfNeeded(newEvent);

            /*
             * Close any existing interval events if the view is being exited
             */
            if (newEvent.eventType == EventTypes.FORM_EXIT) {
                for (Event ev : events) {
                    if (!ev.endTimeSet && ev.isIntervalViewEvent()) {
                        ev.setEnd(start);
                    }
                }
            }

            /*
             * Ignore the event if we are already in an interval view event or have jumped
             * This can happen if the user is on a question page and the page gets refreshed
             * The exception is hierarchy events since they interrupt an existing interval event
             */
            if (newEvent.isIntervalViewEvent()) {
                for (Event ev : events) {
                    if (ev.isIntervalViewEvent() && !ev.endTimeSet) {
                        return;
                    }
                }
            }

            /*
             * Ignore beginning of form events and repeat events
             */
            if (newEvent.eventType == EventTypes.FEC
                    && (newEvent.fecType == FormEntryController.EVENT_BEGINNING_OF_FORM
                    || newEvent.fecType == FormEntryController.EVENT_REPEAT)) {
                return;
            }

            /*
             * Having got to this point we are going to keep the event
             */
            events.add(newEvent);

            /*
             * Write the event unless it is an interval event in which case we need to wait for the end of that event
             */
            if (writeImmediatelyToDisk && !newEvent.isIntervalViewEvent()) {
                writeEvents();
            }
        }

    }

    private void addLocationCoordinatesToEventIfNeeded(Event event) {
        if (audit.isLocationEnabled()) {
            Location location = getMostAccurateLocation();
            String latitude = location != null ? Double.toString(location.getLatitude()) : "";
            String longitude = location != null ? Double.toString(location.getLongitude()) : "";
            String accuracy = location != null ? Double.toString(location.getAccuracy()) : "";
            if (!event.hasLocation()) {
                event.setLocationCoordinates(latitude, longitude, accuracy);
            }
        }
    }

    // If location provider are enabled/disabled it sometimes fires the BroadcastReceiver multiple
    // times what tries to add duplicated logs
    private boolean duplicatedLocationProvidersLog(EventLogger.EventTypes eventType) {
        return (eventType.equals(LOCATION_PROVIDERS_ENABLED) || eventType.equals(LOCATION_PROVIDERS_DISABLED))
                && !events.isEmpty() && eventType.equals(events.get(events.size() - 1).eventType);
    }

    /*
     * Exit a question
     */
    public void exitView() {

        if (auditEnabled) {

            // Calculate the time and add the event to the events array
            long end = getEventTime();
            for (Event ev : events) {
                if (!ev.endTimeSet && ev.isIntervalViewEvent()) {
                    addLocationCoordinatesToEventIfNeeded(ev);
                    ev.setEnd(end);
                }
            }

            writeEvents();
        }
    }

    private void writeEvents() {

        if (saveTask == null || saveTask.getStatus() == AsyncTask.Status.FINISHED) {

            Event[] eventArray = events.toArray(new Event[events.size()]);
            if (auditFile != null) {
                saveTask = new EventSaveTask(auditFile, audit.isLocationEnabled()).execute(eventArray);
            } else {
                Timber.e("auditFile null when attempting to write events.");
            }
            events = new ArrayList<>();

        } else {
            Timber.i("Queueing Event");
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
            List<Location> locationsTemporaryList = new ArrayList<>(locations);

            for (int i = locations.size() - 1; i >= 0; i--) {
                if (System.currentTimeMillis() - locations.get(i).getTime() > audit.getLocationAge()) {
                    locationsTemporaryList.remove(i);
                } else {
                    break;
                }
            }

            locations = new ArrayList<>(locationsTemporaryList);
        }
    }
}