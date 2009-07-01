/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android;


/**
 * The constants used in multiple classes in this application.
 * 
 * @author @author Yaw Anokwa (yanokwa@gmail.com)
 * 
 */
public class SharedConstants {

    /**
     * Request code for returning image capture data from camera intent.
     */
    public static final int IMAGE_CAPTURE = 1;

    /**
     * Request code for returning image capture data from camera intent.
     */
    public static final int BARCODE_CAPTURE = 2;
    
    /**
     * Request code for returning audio data from mediarecorder intent.
     */
    public static final int AUDIO_CAPTURE = 3;
    
    /**
     * Request code for returning video data from mediarecorder intent.
     */
    public static final int VIDEO_CAPTURE = 4;
    
    /**
     * Answer saved with no errors.
     */
    public static final int ANSWER_OK = 0;

    /**
     * Answer required, but was empty or null.
     */
    public static final int ANSWER_REQUIRED_BUT_EMPTY = 1;

    /**
     * Answer constraint was violated.
     */
    public static final int ANSWER_CONSTRAINT_VIOLATED = 2;

    /**
     * Used to validate and display valid form names.
     */
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

    /**
     * Forms storage path
     */
    public static final String FORMS_PATH = "/sdcard/odk/forms/";

    /**
     * Answers storage path
     */
    public static final String ANSWERS_PATH = "/sdcard/odk/answers/";


    /**
     * Identifies the location of the form used to launch form entry
     */
    public static final String FILEPATH_KEY = "formpath";

    /**
     * How long to wait when opening network connection in milliseconds
     */
    public static final int CONNECTION_TIMEOUT = 5000;

    /**
     * Temporary file
     */
    public static final String TMPFILE_PATH = "/sdcard/odk/tmp";

    /**
     * Theme used in entire application
     */
    public static final int APPLICATION_THEME = android.R.style.Theme_Light;

    /**
     * Default font size in entire application
     */
    public final static int APPLICATION_FONTSIZE = 10;
    
    // TODO (carlhartung):  we may need this.  Keeping it until I'm done implementing audio/video.
    /*public final static boolean createTempDirectory(Context context, String className) {
        File tempdir = new File(SharedConstants.TMPFILE_PATH);
        if (!tempdir.exists()) {
            if (!tempdir.mkdirs()) {
                Toast.makeText(context, "Cannot create temporary directory", Toast.LENGTH_LONG).show();
                Log.e(className, "Cannot create directory: " + SharedConstants.TMPFILE_PATH);
                return false;
            }
        }
        return true;
    }*/


}
