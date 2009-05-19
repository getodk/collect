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

package org.google.android.odk;

/**
 * The constants used in multiple classes in this application.
 * 
 * @author Yaw Anokwa
 * 
 */
public class SharedConstants {

    /**
     * Request code for returning image capture data from camera intent.
     */
    public static final int IMAGE_CAPTURE = 1;

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
    public static final String VALID_FORMNAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";

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
    public static final String FORMPATH_KEY = "formpath";
    
    /**
     * How long to wait when opening network connection in milliseconds
     */
    public static final int CONNECTION_TIMEOUT = 5000;
    
    /**
     * Temporary file
     */
    public static final String TMPFILE_PATH = "/sdcard/odk/tmp";
    
    public static final int APPLICATION_THEME =  android.R.style.Theme_Light;
    
    
    public static final float APPLICATION_FONT = 10;

    
}
