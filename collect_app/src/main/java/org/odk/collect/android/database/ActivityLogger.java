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

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.analytics.HitBuilders;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.File;
import java.util.LinkedList;

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

}
