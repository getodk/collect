/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.analytics.HitBuilders;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.ACTIVITY_LOGGER_ANALYTICS;

/**
 * Log all user interface activity into a SQLite database. Logging is disabled by default.
 *
 * The logging database will be "/sdcard/odk/log/activityLog.db"
 *
 * Logging is enabled if the file "/sdcard/odk/log/enabled" exists.
 *
 * @author mitchellsundt@gmail.com
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public final class ActivityLogger {

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper() {
            super(new DatabaseContext(Collect.LOG_PATH), DATABASE_NAME, null, DATABASE_VERSION);
            new File(Collect.LOG_PATH).mkdirs();
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * The minimum delay, in milliseconds, for a scroll action to be considered new.
     */
    private static final long MIN_SCROLL_DELAY = 400L;
    /**
     * The maximum size of the scroll action buffer.  After it reaches this size,
     * it will be flushed.
     */
    private static final int MAX_SCROLL_ACTION_BUFFER_SIZE = 8;

    private static final String DATABASE_TABLE = "log";
    private static final String ENABLE_LOGGING = "enabled";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "activityLog.db";
    // Database columns
    private static final String ID = "_id";
    private static final String TIMESTAMP = "timestamp";
    private static final String DEVICEID = "device_id";
    private static final String CLASS = "class";
    private static final String CONTEXT = "context";
    private static final String ACTION = "action";
    private static final String INSTANCE_PATH = "instance_path";
    private static final String QUESTION = "question";
    private static final String PARAM1 = "param1";
    private static final String PARAM2 = "param2";

    private static final String DATABASE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ("
                    + ID + " integer primary key autoincrement, "
                    + TIMESTAMP + " integer not null, "
                    + DEVICEID + " text not null, "
                    + CLASS + " text not null, "
                    + CONTEXT + " text not null, "
                    + ACTION + " text, "
                    + INSTANCE_PATH + " text, "
                    + QUESTION + " text, "
                    + PARAM1 + " text, "
                    + PARAM2 + " text);";

    private final boolean loggingEnabled;
    private final String deviceId;
    private SQLiteDatabase database;
    private boolean isOpen;
    // We buffer scroll actions to make sure there aren't too many pauses
    // during scrolling.  This list is flushed every time any other type of
    // action is logged.
    private final LinkedList<ContentValues> scrollActions = new LinkedList<ContentValues>();

    public ActivityLogger(String deviceId) {
        this.deviceId = deviceId;
        loggingEnabled = new File(Collect.LOG_PATH, ENABLE_LOGGING).exists();

        if (loggingEnabled) {
            open();

            if (isFirstTime()) {
                sendAnalyticsEvent();
                GeneralSharedPreferences.getInstance().save(ACTIVITY_LOGGER_ANALYTICS, false);
            }
        }
    }

    private void sendAnalyticsEvent() {
        Collect.getInstance()
                .getDefaultTracker()
                .send(new HitBuilders.EventBuilder()
                        .setCategory("ActivityLogger")
                        .setAction("Enabled")
                        .setLabel("ActivityLogger is enabled")
                        .build());
    }

    private boolean isFirstTime() {
        return GeneralSharedPreferences.getInstance().getBoolean(ACTIVITY_LOGGER_ANALYTICS, true);
    }

    public boolean isOpen() {
        return loggingEnabled && isOpen;
    }

    public void open() throws SQLException {
        if (!loggingEnabled || isOpen) {
            return;
        }
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper();
            database = databaseHelper.getWritableDatabase();
            isOpen = true;
        } catch (SQLiteException e) {
            Timber.e(e);
            isOpen = false;
        }
    }

    // cached to improve logging performance...
    // only access these through getXPath(FormIndex index);
    private FormIndex cachedXPathIndex;
    private String cachedXPathValue;

    // DO NOT CALL THIS OUTSIDE OF synchronized(scrollActions) !!!!
    // DO NOT CALL THIS OUTSIDE OF synchronized(scrollActions) !!!!
    // DO NOT CALL THIS OUTSIDE OF synchronized(scrollActions) !!!!
    // DO NOT CALL THIS OUTSIDE OF synchronized(scrollActions) !!!!
    private String getXPath(FormIndex index) {
        if (index == cachedXPathIndex) {
            return cachedXPathValue;
        }

        cachedXPathIndex = index;
        cachedXPathValue = Collect.getInstance().getFormController().getXPath(index);
        return cachedXPathValue;
    }


    private void log(String object, String context, String action, String instancePath,
            FormIndex index, String param1, String param2) {
        if (!isOpen()) {
            return;
        }

        ContentValues cv = new ContentValues();
        cv.put(DEVICEID, deviceId);
        cv.put(CLASS, object);
        cv.put(CONTEXT, context);
        cv.put(ACTION, action);
        cv.put(INSTANCE_PATH, instancePath);
        cv.put(PARAM1, param1);
        cv.put(PARAM2, param2);
        cv.put(TIMESTAMP, Calendar.getInstance().getTimeInMillis());

        insertContentValues(cv, index);
    }

    private String getInstancePath(FormController formController) {
        File f = formController.getInstanceFile();
        if (f == null) {
            return "<not-yet-specified>";
        } else {
            return f.getAbsolutePath();
        }
    }

    public void logScrollAction(Object t, int distance) {
        if (!isOpen()) {
            return;
        }

        synchronized (scrollActions) {
            long timeStamp = Calendar.getInstance().getTimeInMillis();

            // Check to see if we can add this scroll action to the previous action.
            if (!scrollActions.isEmpty()) {
                ContentValues lastCv = scrollActions.get(scrollActions.size() - 1);
                long oldTimeStamp = lastCv.getAsLong(TIMESTAMP);
                int oldDistance = Integer.parseInt(lastCv.getAsString(PARAM1));
                if (Integer.signum(distance) == Integer.signum(oldDistance)
                        && timeStamp - oldTimeStamp < MIN_SCROLL_DELAY) {
                    lastCv.put(PARAM1, oldDistance + distance);
                    lastCv.put(TIMESTAMP, timeStamp);
                    return;
                }
            }

            if (scrollActions.size() >= MAX_SCROLL_ACTION_BUFFER_SIZE) {
                insertContentValues(null, null); // flush scroll list...
            }

            String idx = "";
            String instancePath = null;
            FormController formController = Collect.getInstance().getFormController();
            if (formController != null) {
                idx = getXPath(formController.getFormIndex());
                instancePath = getInstancePath(formController);
            }

            // Add a new scroll action to the buffer.
            ContentValues cv = new ContentValues();
            cv.put(DEVICEID, deviceId);
            cv.put(CLASS, t.getClass().getName());
            cv.put(CONTEXT, "scroll");
            cv.put(ACTION, "");
            cv.put(PARAM1, distance);
            cv.put(QUESTION, idx);
            cv.put(INSTANCE_PATH, instancePath);
            cv.put(TIMESTAMP, timeStamp);
            cv.put(PARAM2, timeStamp);
            scrollActions.add(cv);
        }
    }

    private void insertContentValues(ContentValues cv, FormIndex index) {
        synchronized (scrollActions) {
            try {
                while (!scrollActions.isEmpty()) {
                    ContentValues scv = scrollActions.removeFirst();
                    database.insert(DATABASE_TABLE, null, scv);
                }

                if (cv != null) {
                    String idx = "";
                    if (index != null) {
                        idx = getXPath(index);
                    }
                    cv.put(QUESTION, idx);
                    database.insert(DATABASE_TABLE, null, cv);
                }
            } catch (SQLiteConstraintException e) {
                Timber.e(e);
            }
        }
    }

    // Convenience methods

    public void logOnStart(Activity a) {
        log(a.getClass().getName(), "onStart", null, null, null, null, null);
    }

    public void logOnStop(Activity a) {
        log(a.getClass().getName(), "onStop", null, null, null, null, null);
    }

    public void logAction(Object t, String context, String action) {
        log(t.getClass().getName(), context, action, null, null, null, null);
    }

    public void logActionParam(Object t, String context, String action, String param1) {
        log(t.getClass().getName(), context, action, null, null, param1, null);
    }

    public void logInstanceAction(Object t, String context, String action) {
        FormIndex index = null;
        String instancePath = null;
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            index = formController.getFormIndex();
            instancePath = getInstancePath(formController);
        }
        log(t.getClass().getName(), context, action, instancePath, index, null, null);
    }

    public void logInstanceAction(Object t, String context, String action, FormIndex index) {
        String instancePath = null;
        FormController formController = Collect.getInstance().getFormController();
        if (formController != null) {
            instancePath = getInstancePath(formController);
        }
        log(t.getClass().getName(), context, action, instancePath, index, null, null);
    }
}
