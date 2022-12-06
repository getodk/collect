/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class InstanceProviderAPI {
    public static final String AUTHORITY = "org.odk.collect.android.provider.odk.instances.smap";

    // This class cannot be instantiated
    private InstanceProviderAPI() {
    }

    /**
     * Notes table
     */
    public static final class InstanceColumns implements BaseColumns {
        // This class cannot be instantiated
        private InstanceColumns() {
        }

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/instances");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.instance";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.odk.instance";

        // instance column names
        public static final String DISPLAY_NAME = "displayName";
        public static final String SUBMISSION_URI = "submissionUri";
        public static final String INSTANCE_FILE_PATH = "instanceFilePath";
        public static final String JR_FORM_ID = "jrFormId";
        public static final String SOURCE = "source";			// smap
        public static final String JR_VERSION = "jrVersion";
        public static final String STATUS = "status";
        public static final String CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete";
        public static final String LAST_STATUS_CHANGE_DATE = "date";
        public static final String DELETED_DATE = "deletedDate";

        // Smap Start
        public static final String FORM_PATH = "formPath";          // Path to the form that for this instance
        public static final String ACT_LON = "actLon";              // Actual longitude task was completed
        public static final String ACT_LAT = "actLat";              // Actual latitude task was completed
        public static final String SCHED_LON = "schedLon";          // Scheduled longitude for task
        public static final String SCHED_LAT = "schedLat";          // Scheduled latitude for task
        public static final String T_TITLE = "tTitle";              // Task title
        public static final String T_SCHED_START = "tSchedStart";   // Scheduled Start
        public static final String T_SCHED_FINISH = "tSchedFinish";   // Scheduled Start
        public static final String T_ACT_START = "tActStart";       // Actual Start
        public static final String T_ACT_FINISH = "tActFinish";     // Actual Finish
        public static final String T_ADDRESS = "tAddress";          // Address of task
        public static final String T_IS_SYNC = "tIsSync";           // Set if the instance has been synced
        public static final String T_ASS_ID = "tTaskId";            // Task Id
        public static final String T_TASK_STATUS = "tAssStatus";    // Assignment Status
        public static final String T_TASK_TYPE = "tTaskType";    // Task type case || task
        public static final String T_TASK_COMMENT = "tComment";    // Task comment
        public static final String T_REPEAT = "tRepeat";            // Task can be completed multiple times
        public static final String T_UPDATEID = "tUpdateId";          // The unique identifier of the instance to be updated
        public static final String T_LOCATION_TRIGGER = "tLocationTrigger";  // An NFC UID or Geofence that will trigger the task
        public static final String T_SURVEY_NOTES = "tSurveyNotes";  // Any notes added to the assessment outside of the form itself
        public static final String T_UPDATED = "tUpdated";          // Record the number of times the instance is updated
        public static final String UUID = "uuid";
        public static final String T_SHOW_DIST = "tShowDist";       // Distance at which task will be shown, 0 for always show
        public static final String T_HIDE = "tHide";                // Set true if task is to be hidden from view
        // Smap End

        public static final String GEOMETRY = "tGeom";              // Full geometry for location of task - Smap Aligned with Collect
        public static final String GEOMETRY_TYPE = "tGeomType";     // Geometry type; Polygon, linestring, - Smap Aligned with Collect
        public static final String PHONE = "phone";                 // Phone number of task
    }
}
